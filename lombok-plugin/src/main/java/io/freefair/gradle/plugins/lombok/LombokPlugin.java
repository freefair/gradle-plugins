package io.freefair.gradle.plugins.lombok;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.quality.CodeQualityExtension;
import org.gradle.api.plugins.quality.FindBugsExtension;
import org.gradle.api.plugins.quality.FindBugsPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;

@Getter
public class LombokPlugin implements Plugin<Project> {

    private LombokExtension lombokExtension;
    private Configuration lombokConfiguration;
    private Project project;
    private TaskProvider<GenerateLombokConfig> generateLombokConfig;

    @Override
    public void apply(Project project) {
        this.project = project;
        lombokExtension = project.getExtensions().create("lombok", LombokExtension.class);
        lombokExtension.getConfig().put("config.stopBubbling", "true");

        lombokConfiguration = project.getConfigurations().create("lombok");
        lombokConfiguration.defaultDependencies(dependencySet -> dependencySet.add(
                project.getDependencies().create("org.projectlombok:lombok:" + lombokExtension.getVersion())
        ));

        generateLombokConfig = project.getTasks().register("generateLombokConfig", GenerateLombokConfig.class, genConfig -> {
            genConfig.getProperties().convention(lombokExtension.getConfig());
            genConfig.setGroup("lombok");
        });

        project.getTasks().withType(Delombok.class).configureEach(this::configureDelombokDefaults);

        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> configureJavaPluginDefaults());

    }

    private void configureJavaPluginDefaults() {
        JavaPluginConvention javaPluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class);

        javaPluginConvention.getSourceSets().all(sourceSet -> {
            project.getConfigurations().getByName(sourceSet.getCompileOnlyConfigurationName()).extendsFrom(lombokConfiguration);
            project.getConfigurations().getByName(sourceSet.getAnnotationProcessorConfigurationName()).extendsFrom(lombokConfiguration);

            TaskProvider<Delombok> delombokTaskProvider = project.getTasks().register(sourceSet.getTaskName("delombok", ""), Delombok.class, delombok -> {
                delombok.setDescription("Runs delombok on the " + sourceSet.getName() + " source-set");
                delombok.getTarget().set(project.getLayout().getBuildDirectory().dir("delombok/" + sourceSet.getName()));
            });

            sourceSet.getExtensions().add("delombokTask", delombokTaskProvider);

            TaskProvider<JavaCompile> compileTaskProvider = project.getTasks().named(sourceSet.getCompileJavaTaskName(), JavaCompile.class, compileJava -> {
                compileJava.dependsOn(generateLombokConfig);
                compileJava.getOptions().getCompilerArgs().add("-Xlint:-processing");
                compileJava.getInputs().file(generateLombokConfig.get().getOutputFile());
            });

            project.afterEvaluate(p -> {
                delombokTaskProvider.configure(delombok -> {
                    delombok.getEncoding().set(compileTaskProvider.get().getOptions().getEncoding());
                    delombok.getClasspath().from(sourceSet.getCompileClasspath());
                    delombok.getInput().from(sourceSet.getAllJava().getSourceDirectories());
                });
            });

        });

        project.getTasks().named(JavaPlugin.JAVADOC_TASK_NAME, Javadoc.class, javadoc -> {
            SourceSet mainSourceSet = javaPluginConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
            javadoc.setSource(mainSourceSet.getExtensions().getByName("delombokTask"));
        });

        project.getPlugins().withType(JacocoPlugin.class, jacocoPlugin -> configureForJacoco());

        project.getPlugins().withType(FindBugsPlugin.class, findBugsPlugin -> configureForFindbugs(javaPluginConvention));

        project.getPlugins().withId("com.github.spotbugs", spotBugsPlugin -> configureForSpotbugs(javaPluginConvention));
    }

    private void configureForSpotbugs(JavaPluginConvention javaPluginConvention) {
        lombokExtension.getConfig().put("lombok.extern.findbugs.addSuppressFBWarnings", "true");

        CodeQualityExtension spotbugsExtension = (CodeQualityExtension) project.getExtensions().getByName("spotbugs");
        javaPluginConvention.getSourceSets().all(sourceSet ->
                project.getDependencies().add(
                        sourceSet.getCompileOnlyConfigurationName(),
                        "com.github.spotbugs:spotbugs-annotations:" + spotbugsExtension.getToolVersion()
                ));
    }

    private void configureForFindbugs(JavaPluginConvention javaPluginConvention) {
        lombokExtension.getConfig().put("lombok.extern.findbugs.addSuppressFBWarnings", "true");

        FindBugsExtension findBugsExtension = project.getExtensions().getByType(FindBugsExtension.class);
        javaPluginConvention.getSourceSets().all(sourceSet ->
                project.getDependencies().add(
                        sourceSet.getCompileOnlyConfigurationName(),
                        "com.google.code.findbugs:findbugs:" + findBugsExtension.getToolVersion()
                ));
    }

    private void configureForJacoco() {
        lombokExtension.getConfig().put("lombok.addLombokGeneratedAnnotation", "true");
    }

    private void configureDelombokDefaults(Delombok delombok) {
        delombok.getLombokClasspath().from(lombokConfiguration);
        delombok.setGroup("lombok");
        delombok.getFormat().put("pretty", null);
    }
}
