package io.freefair.gradle.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.compile.JavaCompile;

public class AspectJPostCompileWeavingPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        AspectJBasePlugin aspectjBasePlugin = project.getPlugins().apply(AspectJBasePlugin.class);

        project.getPlugins().apply(JavaPlugin.class);

        project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(sourceSet -> {
            project.getDependencies().add(sourceSet.getCompileConfigurationName(), "org.aspectj:aspectjrt:" + aspectjBasePlugin.getAspectjExtension().getVersion().get());

            Configuration aspects = project.getConfigurations().create(sourceSet.getTaskName(null, "aspects"));

            project.getConfigurations().getByName(sourceSet.getCompileConfigurationName()).extendsFrom(aspects);

            String taskName = sourceSet.getTaskName("weave", "Aspects");

            Ajc ajc = project.getTasks().create(taskName, Ajc.class);

            JavaCompile compileJava = (JavaCompile) project.getTasks().getByName(sourceSet.getCompileJavaTaskName());

            Provider<Directory> dir = project.getLayout().getBuildDirectory().dir("javac/" + sourceSet.getName());
            compileJava.setDestinationDir(dir.map(Directory::getAsFile));

            ajc.getSource().set(project.provider(compileJava::getSourceCompatibility));
            ajc.getTarget().set(project.provider(compileJava::getTargetCompatibility));
            ajc.getClasspath().from(compileJava.getClasspath());
            ajc.getAspectpath().from(aspects);
            ajc.getInpath().from(compileJava.getDestinationDir()).builtBy(compileJava);
            ajc.getDestinationDir().set(sourceSet.getJava().getOutputDir());

            project.getTasks().getByName(sourceSet.getClassesTaskName()).dependsOn(ajc);
        });
    }
}
