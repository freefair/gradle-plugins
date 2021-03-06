package io.freefair.gradle.plugins.lombok;

import io.freefair.gradle.plugins.lombok.tasks.CheckLombokConfig;
import io.freefair.gradle.plugins.lombok.tasks.Delombok;
import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.quality.CodeQualityExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Getter
public class LombokPlugin implements Plugin<Project> {

    private static final String LOMBOK_MAPSTRUCT_VERSION = "0.2.0";
    private static final String SPOTBUG_DEFAULT_VERSION = "4.1.3";

    private LombokBasePlugin lombokBasePlugin;
    private Project project;

    private boolean spotbugConfigured;
    private TaskProvider<Task> checkLombokConfigs;

    @Override
    public void apply(Project project) {
        this.project = project;

        lombokBasePlugin = project.getPlugins().apply(LombokBasePlugin.class);

        project.getTasks().withType(Delombok.class).configureEach(this::configureDelombokDefaults);

        checkLombokConfigs = project.getTasks().register("checkLombokConfigs");
        project.getPlugins().withType(LifecycleBasePlugin.class, lifecycleBasePlugin -> {
            project.getTasks().named(LifecycleBasePlugin.CHECK_TASK_NAME)
                    .configure(check -> check.dependsOn(checkLombokConfigs));
        });

        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> configureJavaPluginDefaults());

    }

    private void configureJavaPluginDefaults() {
        JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);

        javaPluginExtension.getSourceSets().all(this::configureSourceSetDefaults);

        project.getTasks().named(JavaPlugin.JAVADOC_TASK_NAME, Javadoc.class, javadoc -> {
            SourceSet mainSourceSet = javaPluginExtension.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
            javadoc.setSource(mainSourceSet.getExtensions().getByName("delombokTask"));
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

            handleLombokConfig(sourceSet, compileTaskProvider);

            delombokTaskProvider.configure(delombok -> {
                delombok.getEncoding().set(compileTaskProvider.get().getOptions().getEncoding());
                delombok.getClasspath().from(sourceSet.getCompileClasspath());
                delombok.getInput().from(sourceSet.getJava().getSourceDirectories());
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
            return SPOTBUG_DEFAULT_VERSION;
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

    private void handleLombokConfig(SourceSet sourceSet, TaskProvider<JavaCompile> compileTaskProvider) {
        Map<File, Provider<String>> lombokConfigs = new HashMap<>();

        for (File srcDir : sourceSet.getJava().getSrcDirs()) {
            Provider<String> lombokConfig = getLombokConfigProvider(srcDir);
            lombokConfigs.put(srcDir, lombokConfig);
        }

        TaskProvider<CheckLombokConfig> checkLombokConfig = project.getTasks()
                .register(sourceSet.getTaskName("check", "lombokConfig"), CheckLombokConfig.class, sourceSet);

        checkLombokConfig.configure(c -> {
            lombokConfigs.forEach((file, stringProvider) -> {
                c.getConfigs().put(file, stringProvider);
            });
        });

        checkLombokConfigs.configure(c -> c.dependsOn(checkLombokConfig));

        compileTaskProvider.configure(javaCompile -> {
            javaCompile.getInputs()
                    .property("lombokConfig", lombokConfigs)
                    .optional(true);
            javaCompile.dependsOn(checkLombokConfig);
        });

        project.getPlugins().withType(JacocoPlugin.class, jacocoPlugin -> {
            checkLombokConfig.configure(c -> c.getExpectedConfigs().add("lombok.addLombokGeneratedAnnotation = true"));
        });

        project.getPlugins().withId("com.github.spotbugs", spotBugsPlugin -> {
            checkLombokConfig.configure(c -> c.getExpectedConfigs().add("lombok.extern.findbugs.addSuppressFBWarnings = true"));
        });

        project.getPlugins().withId("org.sonarqube", spotBugsPlugin -> {
            checkLombokConfig.configure(c -> c.getExpectedConfigs().add("lombok.extern.findbugs.addSuppressFBWarnings = true"));
        });
    }

    private Provider<String> getLombokConfigProvider(File dir) {
        return project.provider(() -> getLombokConfig(dir));
    }

    @Nullable
    private String getLombokConfig(File dir) {
        if (!dir.exists()) {
            return null;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        project.javaexec(configTool -> {
            configTool.setClasspath(lombokBasePlugin.getLombokConfiguration());
            configTool.setStandardOutput(outputStream);

            configTool.args("config", dir);
        });

        String output = outputStream.toString();
        project.getLogger().debug(output);

        if (output.startsWith("No 'lombok.config' found for")) {
            return null;
        }
        return output;
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
