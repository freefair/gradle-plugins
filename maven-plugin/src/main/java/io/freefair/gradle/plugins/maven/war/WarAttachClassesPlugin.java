package io.freefair.gradle.plugins.maven.war;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.War;

public class WarAttachClassesPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPlugins().apply(WarPlugin.class);

        Property<Boolean> attachClasses = project.getObjects().property(Boolean.class).convention(false);
        Property<String> classesClassifier = project.getObjects().property(String.class).convention("classes");

        project.getTasks().named(WarPlugin.WAR_TASK_NAME, War.class, war -> {
            war.getExtensions().add("attachClasses", attachClasses);
            war.getExtensions().add("classesClassifier", classesClassifier);
        });

        project.afterEvaluate(p -> {
            if (attachClasses.get()) {
                TaskProvider<Jar> jar = project.getTasks().named(JavaPlugin.JAR_TASK_NAME, Jar.class, j ->
                        j.getArchiveClassifier().convention(classesClassifier)
                );

                project.getArtifacts().add(Dependency.ARCHIVES_CONFIGURATION, jar);
            }
        });
    }
}
