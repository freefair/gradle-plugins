package io.freefair.gradle.plugins.jsass;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Copy;

import java.io.File;

/**
 * @author Lars Grefer
 */
@Getter
public class JSassJavaPlugin implements Plugin<Project> {

    private JSassBasePlugin jSassBasePlugin;

    @Override
    public void apply(Project project) {
        jSassBasePlugin = project.getPlugins().apply(JSassBasePlugin.class);

        project.getPlugins().apply(JavaPlugin.class);

        File baseDestinationDir = new File(project.getBuildDir(), "jsass");

        project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(sourceSet -> {
            String taskName = sourceSet.getCompileTaskName("Sass");

            SassCompile sassCompile = project.getTasks().create(taskName, SassCompile.class);

            sassCompile.setSource(sourceSet.getResources());
            sassCompile.getDestinationDir().set(new File(baseDestinationDir, sourceSet.getName()));
            sassCompile.setGroup(BasePlugin.BUILD_GROUP);
            sassCompile.setDescription("Compile sass and scss files for the " + sourceSet.getName() + " source set");

            Copy processResources = (Copy) project.getTasks().getByName(sourceSet.getProcessResourcesTaskName());

            processResources.from(sassCompile);
        });
    }
}
