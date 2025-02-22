package io.freefair.gradle.plugins.lombok;

import io.freefair.gradle.plugins.lombok.internal.ConfigUtil;
import io.freefair.gradle.plugins.lombok.tasks.Delombok;
import io.freefair.gradle.plugins.lombok.tasks.LombokConfig;
import io.freefair.gradle.plugins.lombok.tasks.LombokTask;
import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.attributes.VerificationType;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.quality.CodeQualityExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;

@Getter
public class LombokPlugin implements Plugin<Project> {

    private static final String LOMBOK_MAPSTRUCT_VERSION = "0.2.0";
    private static final String SPOTBUGS_DEFAULT_VERSION = "4.7.3";

    private LombokBasePlugin lombokBasePlugin;
    private Project project;

    private boolean spotbugConfigured;

    @Override
    public void apply(Project project) {
        this.project = project;

        lombokBasePlugin = project.getPlugins().apply(LombokBasePlugin.class);

        project.getTasks().withType(Delombok.class).configureEach(this::configureDelombokDefaults);

        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> configureJavaPluginDefaults());

    }

    private void configureJavaPluginDefaults() {
        JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);

        project.getTasks().withType(LombokTask.class).configureEach(lombokTask -> {
            JavaToolchainService javaToolchainService = project.getExtensions().getByType(JavaToolchainService.class);
            Provider<JavaLauncher> launcherProvider = javaToolchainService.launcherFor(javaPluginExtension.getToolchain());
            lombokTask.getLauncher().convention(launcherProvider);
        });

        javaPluginExtension.getSourceSets().all(this::configureSourceSetDefaults);

        SourceSet mainSourceSet = javaPluginExtension.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        Object mainDelombokTask = mainSourceSet.getExtensions().getByName("delombokTask");

        project.getTasks().named(JavaPlugin.JAVADOC_TASK_NAME, Javadoc.class, javadoc -> {
            javadoc.setSource(mainDelombokTask);
        });

        Configuration mainSourceElements = project.getConfigurations().getByName("mainSourceElements");

        mainSourceElements.getOutgoing().getVariants().create("delombok", delombokVariant -> {
            delombokVariant.artifact(mainDelombokTask, cpa -> cpa.setType("directory"));
            delombokVariant.getAttributes().attribute(DocsType.DOCS_TYPE_ATTRIBUTE, project.getObjects().named(DocsType.class, DocsType.SOURCES));
        });

        project.getPlugins().withId("com.github.spotbugs", spotBugsPlugin -> configureForSpotbugs(javaPluginExtension));
        project.getPlugins().withId("org.sonarqube", sonarPlugin -> configureForSpotbugs(javaPluginExtension));
    }

    private void configureSourceSetDefaults(SourceSet sourceSet) {
        project.getConfigurations().getByName(sourceSet.getCompileOnlyConfigurationName()).extendsFrom(lombokBasePlugin.getLombokConfiguration());
        project.getConfigurations().getByName(sourceSet.getAnnotationProcessorConfigurationName()).extendsFrom(lombokBasePlugin.getLombokConfiguration());

        TaskProvider<Delombok> delombokTaskProvider = project.getTasks().register(sourceSet.getTaskName("delombok", ""), Delombok.class, delombok -> {
            delombok.setDescription("Runs delombok on the " + sourceSet.getName() + " source-set");
            String delombokDir = "generated/sources/delombok/" + sourceSet.getJava().getName() + "/" + sourceSet.getName();
            delombok.getTarget().convention(project.getLayout().getBuildDirectory().dir(delombokDir));
        });

        sourceSet.getExtensions().add("delombokTask", delombokTaskProvider);

        TaskProvider<JavaCompile> compileTaskProvider = project.getTasks().named(sourceSet.getCompileJavaTaskName(), JavaCompile.class, compileJava -> {
            compileJava.getOptions().getCompilerArgs().add("-Xlint:-processing");
        });

        project.afterEvaluate(p -> {
            handleMapstructSupport(sourceSet);

            handleLombokConfig(sourceSet, compileTaskProvider, delombokTaskProvider);

            delombokTaskProvider.configure(delombok -> {
                delombok.getEncoding().set(compileTaskProvider.map(c -> c.getOptions().getEncoding()));
                delombok.getClasspath().from(sourceSet.getCompileClasspath());
                delombok.getInput().from(sourceSet.getJava().getSourceDirectories());
                delombok.getSourcepath().from(compileTaskProvider.flatMap(c -> c.getOptions().getGeneratedSourceOutputDirectory()));
                delombok.dependsOn(sourceSet.getJava().getBuildDependencies());
            });
        });

    }

    private void configureForSpotbugs(JavaPluginExtension javaPluginExtension) {
        if (spotbugConfigured) {
            return;
        }
        spotbugConfigured = true;

        project.afterEvaluate(p -> {
            String toolVersion = resolveSpotBugVersion();

            javaPluginExtension.getSourceSets().all(sourceSet ->
                    project.getDependencies().add(
                            sourceSet.getCompileOnlyConfigurationName(),
                            "com.github.spotbugs:spotbugs-annotations:" + toolVersion
                    ));
        });
    }

    private String resolveSpotBugVersion() {
        if (!project.getPlugins().hasPlugin("com.github.spotbugs")) {
            return SPOTBUGS_DEFAULT_VERSION;
        }

        Object spotbugsExtension = project.getExtensions().getByName("spotbugs");
        if (spotbugsExtension instanceof CodeQualityExtension) {
            return ((CodeQualityExtension) spotbugsExtension).getToolVersion();
        }

        Property<String> toolVersionProperty = (Property<String>) new DslObject(spotbugsExtension).getAsDynamicObject().getProperty("toolVersion");
        return toolVersionProperty.get();
    }

    private void configureDelombokDefaults(Delombok delombok) {
        delombok.setGroup("lombok");
        delombok.getFormat().put("pretty", null);
    }

    private void handleLombokConfig(SourceSet sourceSet, TaskProvider<JavaCompile> compileTaskProvider, TaskProvider<Delombok> delombokTaskProvider) {
        if (lombokBasePlugin.getLombokExtension().getDisableConfig().get()) {
            return;
        }

        TaskProvider<LombokConfig> lombokConfigTask = ConfigUtil.getLombokConfigTask(project, sourceSet);

        compileTaskProvider.configure(javaCompile -> {
            javaCompile.getInputs().file(lombokConfigTask.get().getOutputFile())
                    .withPropertyName("lombok.config")
                    .withPathSensitivity(PathSensitivity.NONE)
                    .optional();
        });

        delombokTaskProvider.configure(delombok -> {
            delombok.getInputs().file(lombokConfigTask.get().getOutputFile())
                    .withPropertyName("lombok.config")
                    .withPathSensitivity(PathSensitivity.NONE)
                    .optional();
        });
    }

    private void handleMapstructSupport(SourceSet sourceSet) {
        Configuration annotationProcessor = project.getConfigurations().getByName(sourceSet.getAnnotationProcessorConfigurationName());

        Dependency mapstruct = null;
        boolean hasBinding = false;

        for (Dependency aptDependency : annotationProcessor.getAllDependencies()) {
            if ("mapstruct-processor".equals(aptDependency.getName()) && "org.mapstruct".equals(aptDependency.getGroup())) {
                mapstruct = aptDependency;
            }

            if ("lombok-mapstruct-binding".equals(aptDependency.getName())) {
                hasBinding = true;
            }
        }

        if (mapstruct != null && !hasBinding) {
            project.getLogger().info("Adding lombok-mapstruct-binding for source set {} because {} was found", sourceSet.getName(), mapstruct);

            project.getDependencies().add(
                    sourceSet.getAnnotationProcessorConfigurationName(),
                    "org.projectlombok:lombok-mapstruct-binding:" + LOMBOK_MAPSTRUCT_VERSION
            );
        }
    }
}
