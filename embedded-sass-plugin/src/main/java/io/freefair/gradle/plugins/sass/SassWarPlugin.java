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

/**
 * Plugin that integrates Sass compilation with WAR projects.
 * <p>
 * Applies {@link SassWebjarsPlugin} and Gradle's {@link WarPlugin}, then registers a
 * {@code compileWebappSass} task that compiles Sass/SCSS files from the webapp directory
 * ({@code src/main/webapp}) and includes the compiled CSS in the WAR file.
 * <p>
 * The compiled output is placed in the WAR at the same relative path as the source files.
 */
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
