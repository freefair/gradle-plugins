package io.freefair.gradle.plugins.sass;

import org.gradle.api.Incubating;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.War;

import java.io.File;

@Incubating
public class SassWarPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getPlugins().apply(SassWebjarsPlugin.class);
        project.getPlugins().apply(WarPlugin.class);

        TaskProvider<War> warTask = project.getTasks().named("war", War.class);

        TaskProvider<SassCompile> sassCompileTaskProvider = project.getTasks().register("compileWebappSass", SassCompile.class, compileWebappSass -> {
            compileWebappSass.setGroup(BasePlugin.BUILD_GROUP);
            compileWebappSass.setDescription("Compile sass and scss files for the webapp");

            compileWebappSass.source(warTask.flatMap(War::getWebAppDirectory));

            compileWebappSass.getDestinationDir().set(project.getLayout().getBuildDirectory().dir("sass/webapp"));
        });

        warTask.configure(war -> war.from(sassCompileTaskProvider));
    }
}
