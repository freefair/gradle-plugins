package io.freefair.gradle.plugins.maven.war;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.War;

public class WarAttachClassesPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPlugins().apply(WarPlugin.class);

        WarAttachClassesConvention attachClassesConvention = new WarAttachClassesConvention();

        project.getTasks().named(WarPlugin.WAR_TASK_NAME, War.class, war ->
                war.getConvention().getPlugins().put("attachClasses", attachClassesConvention)
        );

        project.afterEvaluate(p -> {
            if (attachClassesConvention.isAttachClasses()) {
                TaskProvider<Jar> jar = project.getTasks().named(JavaPlugin.JAR_TASK_NAME, Jar.class, j ->
                        j.getArchiveClassifier().convention(attachClassesConvention.getClassesClassifier())
                );

                project.getArtifacts().add(Dependency.ARCHIVES_CONFIGURATION, jar);
            }
        });
    }
}
