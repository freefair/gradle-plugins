package io.freefair.gradle.plugins.lombok;

import io.freefair.gradle.plugins.lombok.tasks.Delombok;
import io.freefair.gradle.plugins.lombok.tasks.GenerateLombokConfig;
import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.quality.CodeQualityExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;

import java.util.concurrent.Callable;

@Getter
public class LombokPlugin implements Plugin<Project> {

    private LombokBasePlugin lombokBasePlugin;
    private Project project;
    private TaskProvider<GenerateLombokConfig> generateLombokConfig;

    @Override
    public void apply(Project project) {
        this.project = project;

        lombokBasePlugin = project.getPlugins().apply(LombokBasePlugin.class);
        lombokBasePlugin.getLombokExtension().getConfig().put("config.stopBubbling", "true");

        generateLombokConfig = project.getTasks().register("generateLombokConfig", GenerateLombokConfig.class, genConfig -> {
            genConfig.getProperties().convention(lombokBasePlugin.getLombokExtension().getConfig());
            genConfig.setGroup("lombok");
        });

        project.getTasks().withType(Delombok.class).configureEach(this::configureDelombokDefaults);

        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> configureJavaPluginDefaults());

    }

    private void configureJavaPluginDefaults() {
        JavaPluginConvention javaPluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class);

        javaPluginConvention.getSourceSets().all(sourceSet -> {
            project.getConfigurations().getByName(sourceSet.getCompileOnlyConfigurationName()).extendsFrom(lombokBasePlugin.getLombokConfiguration());
            project.getConfigurations().getByName(sourceSet.getAnnotationProcessorConfigurationName()).extendsFrom(lombokBasePlugin.getLombokConfiguration());

            TaskProvider<Delombok> delombokTaskProvider = project.getTasks().register(sourceSet.getTaskName("delombok", ""), Delombok.class, delombok -> {
                delombok.setDescription("Runs delombok on the " + sourceSet.getName() + " source-set");
                delombok.getTarget().set(project.getLayout().getBuildDirectory().dir("delombok/" + sourceSet.getName()));
            });

            sourceSet.getExtensions().add("delombokTask", delombokTaskProvider);

            TaskProvider<JavaCompile> compileTaskProvider = project.getTasks().named(sourceSet.getCompileJavaTaskName(), JavaCompile.class, compileJava -> {
                compileJava.dependsOn(generateLombokConfig);
                compileJava.getOptions().getCompilerArgs().add("-Xlint:-processing");
                compileJava.getInputs().file((Callable<RegularFileProperty>) () -> {
                    GenerateLombokConfig generateLombokConfig = this.generateLombokConfig.get();
                    if (generateLombokConfig.isEnabled()) {
                        return generateLombokConfig.getOutputFile();
                    }
                    else {
                        return null;
                    }
                }).withPropertyName("lombok.config").optional();
            });

            project.afterEvaluate(p -> {
                delombokTaskProvider.configure(delombok -> {
                    delombok.getEncoding().set(compileTaskProvider.get().getOptions().getEncoding());
                    delombok.getClasspath().from(sourceSet.getCompileClasspath());
                    delombok.getInput().from(sourceSet.getJava().getSourceDirectories());
                });
            });

        });

        project.getTasks().named(JavaPlugin.JAVADOC_TASK_NAME, Javadoc.class, javadoc -> {
            SourceSet mainSourceSet = javaPluginConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
            javadoc.setSource(mainSourceSet.getExtensions().getByName("delombokTask"));
        });

        project.getPlugins().withType(JacocoPlugin.class, jacocoPlugin -> configureForJacoco());

        project.getPlugins().withId("com.github.spotbugs", spotBugsPlugin -> configureForSpotbugs(javaPluginConvention));
    }

    private void configureForSpotbugs(JavaPluginConvention javaPluginConvention) {
        lombokBasePlugin.getLombokExtension().getConfig().put("lombok.extern.findbugs.addSuppressFBWarnings", "true");

        project.afterEvaluate(p -> {
            Object spotbugsExtension = project.getExtensions().getByName("spotbugs");

            String toolVersion;
            if (spotbugsExtension instanceof CodeQualityExtension) {
                toolVersion = ((CodeQualityExtension) spotbugsExtension).getToolVersion();
            }
            else {
                Property<String> toolVersionProperty = (Property<String>) new DslObject(spotbugsExtension).getAsDynamicObject().getProperty("toolVersion");
                toolVersion = toolVersionProperty.get();
            }

            javaPluginConvention.getSourceSets().all(sourceSet ->
                    project.getDependencies().add(
                            sourceSet.getCompileOnlyConfigurationName(),
                            "com.github.spotbugs:spotbugs-annotations:" + toolVersion
                    ));
        });
    }

    private void configureForJacoco() {
        lombokBasePlugin.getLombokExtension().getConfig().put("lombok.addLombokGeneratedAnnotation", "true");
    }

    private void configureDelombokDefaults(Delombok delombok) {
        delombok.setGroup("lombok");
        delombok.getFormat().put("pretty", null);
    }
}
