package io.freefair.gradle.plugins.jsass;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.deprecation.DeprecationLogger;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.io.File;

/**
 * @author Lars Grefer
 */
@Getter
@Deprecated
public class JSassJavaPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getPlugins().apply(JSassWebjarsPlugin.class);

        project.getPlugins().apply(JavaPlugin.class);

        File baseDestinationDir = new File(project.getBuildDir(), "jsass");

        project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets().all(sourceSet -> {
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
