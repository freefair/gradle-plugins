package io.freefair.gradle.plugins.gwt;

import io.freefair.gradle.plugins.gwt.tasks.GwtCompileTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.War;

/**
 * @author Lars Grefer
 */
public class GwtWarPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getPlugins().apply(WarPlugin.class);
        project.getPlugins().apply(GwtBasePlugin.class);

        TaskProvider<War> warTask = project.getTasks().named(WarPlugin.WAR_TASK_NAME, War.class);
        TaskProvider<GwtCompileTask> gwtCompile = project.getTasks().named("gwtCompile", GwtCompileTask.class);

        warTask.configure(war -> {
            war.from(gwtCompile.flatMap(GwtCompileOptions::getWar));
        });

        gwtCompile.configure(task -> {
            task.getDeploy().unsetConvention();
            task.getDeploy().unset();
        });

    }
}
