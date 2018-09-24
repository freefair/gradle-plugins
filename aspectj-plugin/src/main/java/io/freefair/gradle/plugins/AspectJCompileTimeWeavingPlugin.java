package io.freefair.gradle.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.compile.JavaCompile;

public class AspectJCompileTimeWeavingPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {

        AspectJBasePlugin aspectjBasePlugin = project.getPlugins().apply(AspectJBasePlugin.class);

        project.getPlugins().apply(JavaPlugin.class);

        JavaPluginConvention javaPluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class);

        javaPluginConvention.getSourceSets().all(sourceSet -> {
            project.afterEvaluate(p ->
                    p.getDependencies().add(sourceSet.getCompileConfigurationName(), "org.aspectj:aspectjrt:" + aspectjBasePlugin.getAspectjExtension().getVersion().get())
            );

            Configuration aspects = project.getConfigurations().create(sourceSet.getTaskName(null, "aspects"));

            project.getConfigurations().getByName(sourceSet.getCompileConfigurationName()).extendsFrom(aspects);

            String taskName = sourceSet.getTaskName("ajc", "Java");

            Ajc ajc = project.getTasks().create(taskName, Ajc.class);

            JavaCompile compileJava = (JavaCompile) project.getTasks().getByName(sourceSet.getCompileJavaTaskName());

            compileJava.setEnabled(false);

            ajc.getFiles().from(sourceSet.getJava());
            ajc.getDestinationDir().set(sourceSet.getJava().getOutputDir());
            ajc.getClasspath().from(sourceSet.getCompileClasspath());
            ajc.getAspectpath().from(aspects);

            project.getTasks().getByName(sourceSet.getClassesTaskName()).dependsOn(ajc);
        });

    }
}
