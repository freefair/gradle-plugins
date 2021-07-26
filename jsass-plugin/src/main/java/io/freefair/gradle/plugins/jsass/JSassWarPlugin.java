package io.freefair.gradle.plugins.jsass;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.War;
import org.gradle.internal.deprecation.DeprecationLogger;

import java.io.File;

@Deprecated
public class JSassWarPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getPlugins().apply(JSassWebjarsPlugin.class);
        project.getPlugins().apply(WarPlugin.class);

        TaskProvider<SassCompile> sassCompileTaskProvider = project.getTasks().register("compileWebappSass", SassCompile.class, compileWebappSass -> {
            compileWebappSass.setGroup(BasePlugin.BUILD_GROUP);
            compileWebappSass.setDescription("Compile sass and scss files for the webapp");

            Provider<DirectoryProperty> webAppDir = project.getTasks()
                    .named(WarPlugin.WAR_TASK_NAME, War.class)
                    .map(War::getWebAppDirectory);
            compileWebappSass.source(webAppDir);

            compileWebappSass.getDestinationDir().set(new File(project.getBuildDir(), "jsass/webapp"));
        });

        project.getTasks().named(WarPlugin.WAR_TASK_NAME, War.class)
                .configure(war -> war.from(sassCompileTaskProvider));
    }
}
