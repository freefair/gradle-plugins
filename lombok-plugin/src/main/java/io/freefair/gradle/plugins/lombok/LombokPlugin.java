package io.freefair.gradle.plugins.lombok;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.quality.CodeQualityExtension;
import org.gradle.api.plugins.quality.FindBugsExtension;
import org.gradle.api.plugins.quality.FindBugsPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;

@Getter
public class LombokPlugin implements Plugin<Project> {

    private LombokExtension lombokExtension;
    private Configuration lombokConfiguration;
    private Project project;
    private GenerateLombokConfig generateLombokConfig;

    @Override
    public void apply(Project project) {
        this.project = project;
        lombokExtension = project.getExtensions().create("lombok", LombokExtension.class);
        lombokConfiguration = project.getConfigurations().create("lombok");

        lombokConfiguration.defaultDependencies(dependencySet -> dependencySet.add(
                project.getDependencies().create("org.projectlombok:lombok:" + lombokExtension.getVersion())
        ));

        generateLombokConfig = project.getTasks().create("generateLombokConfig", GenerateLombokConfig.class);
        generateLombokConfig.setProperties(lombokExtension.getConfig());
        generateLombokConfig.setGroup("lombok");

        project.getTasks().withType(Delombok.class, this::configureDelombokDefaults);

        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> configureJavaPluginDefaults());

    }

    private void configureJavaPluginDefaults() {
        JavaPluginConvention javaPluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class);

        javaPluginConvention.getSourceSets().all(sourceSet -> {
            project.getConfigurations().getByName(sourceSet.getCompileOnlyConfigurationName()).extendsFrom(lombokConfiguration);
            project.getConfigurations().getByName(sourceSet.getAnnotationProcessorConfigurationName()).extendsFrom(lombokConfiguration);

            Delombok delombok = project.getTasks().create(sourceSet.getTaskName("delombok", ""), Delombok.class);
            delombok.setDescription("Runs delombok on the " + sourceSet.getName() + " source-set");

            ((ExtensionAware) sourceSet).getExtensions().add("delombokTask", delombok);

            JavaCompile compileJava = (JavaCompile) project.getTasks().getByName(sourceSet.getCompileJavaTaskName());
            compileJava.dependsOn(generateLombokConfig);
            compileJava.getOptions().getCompilerArgs().add("-Xlint:-processing");
            project.afterEvaluate(p -> {
                delombok.getEncoding().set(compileJava.getOptions().getEncoding());
                delombok.getClasspath().from(sourceSet.getCompileClasspath());
                compileJava.getInputs().file(generateLombokConfig.getOutputFile());
                delombok.setInput(sourceSet.getAllJava());
            });

            delombok.getTarget().set(project.getLayout().getBuildDirectory().dir("delombok/" + sourceSet.getName()));
        });

        Javadoc javadoc = (Javadoc) project.getTasks().getByName(JavaPlugin.JAVADOC_TASK_NAME);
        SourceSet mainSourceSet = javaPluginConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        javadoc.setSource(((ExtensionAware) mainSourceSet).getExtensions().getByName("delombokTask"));

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

    private String configureForJacoco() {
        return lombokExtension.getConfig().put("lombok.addLombokGeneratedAnnotation", "true");
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    private void configureDelombokDefaults(Delombok delombok) {
        delombok.getLombokClasspath().from(lombokConfiguration);
        delombok.setGroup("lombok");
        delombok.getFormat().put("pretty", null);
    }
}
