package io.freefair.gradle.plugins.gwt;

import io.freefair.gradle.plugins.gwt.tasks.GwtCompileTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.jvm.tasks.ProcessResources;

/**
 * @author Lars Grefer
 */
public class GwtWebJarPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getPlugins().apply(GwtBasePlugin.class);

        TaskProvider<GwtCompileTask> gwtCompile = project.getTasks().named("gwtCompile", GwtCompileTask.class);

        project.getTasks()
                .named(JavaPlugin.PROCESS_RESOURCES_TASK_NAME, ProcessResources.class)
                .configure(processResources -> {
                    processResources.into("META-INF/resources", resources -> {
                        resources.from(gwtCompile.flatMap(GwtCompileOptions::getWar));
                    });

                    processResources.into("deploy", resources -> {
                        resources.from(gwtCompile.flatMap(GwtCompileOptions::getDeploy));
                    });
                });

    }
}
