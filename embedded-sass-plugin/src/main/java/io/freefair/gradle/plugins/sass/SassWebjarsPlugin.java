package io.freefair.gradle.plugins.sass;

import de.larsgrefer.sass.embedded.importer.WebjarsImporter;
import org.gradle.api.Incubating;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

@Incubating
public class SassWebjarsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(SassBasePlugin.class);
        Configuration webjars = project.getConfigurations().create("webjars");

        project.getPlugins().withType(JavaPlugin.class, javaPlugin ->
                webjars.extendsFrom(project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME))
        );

        project.getTasks().withType(SassCompile.class)
                .configureEach(sassCompile -> sassCompile.getWebjars().from(webjars));
    }
}
