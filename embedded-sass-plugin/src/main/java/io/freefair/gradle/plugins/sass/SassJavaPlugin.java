package io.freefair.gradle.plugins.sass;

import lombok.Getter;
import org.gradle.api.Incubating;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.io.File;

/**
 * @author Lars Grefer
 */
@Getter
@Incubating
public class SassJavaPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(SassWebjarsPlugin.class);

        project.getPlugins().apply(JavaPlugin.class);

        File baseDestinationDir = new File(project.getBuildDir(), "sass");

        project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(sourceSet -> {
            String taskName = sourceSet.getCompileTaskName("Sass");

            TaskProvider<SassCompile> sassCompileTaskProvider = project.getTasks().register(taskName, SassCompile.class, sassCompile -> {
                sassCompile.setSource(sourceSet.getResources());
                sassCompile.getDestinationDir().set(new File(baseDestinationDir, sourceSet.getName()));
                sassCompile.setGroup(BasePlugin.BUILD_GROUP);
                sassCompile.setDescription("Compile sass and scss files for the " + sourceSet.getName() + " source set");
            });

            project.getTasks().named(sourceSet.getProcessResourcesTaskName(), ProcessResources.class)
                    .configure(processResources -> processResources.from(sassCompileTaskProvider));
        });
    }
}
