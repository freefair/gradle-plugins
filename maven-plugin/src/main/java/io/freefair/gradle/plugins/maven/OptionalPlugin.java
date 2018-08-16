package io.freefair.gradle.plugins.maven;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;

public class OptionalPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
            Configuration optional = project.getConfigurations().create("optional");

            project.getConfigurations().getByName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME)
                    .extendsFrom(optional);

            project.getConfigurations().getByName(JavaPlugin.TEST_COMPILE_ONLY_CONFIGURATION_NAME)
                    .extendsFrom(optional);
            project.getConfigurations().getByName(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME)
                    .extendsFrom(optional);
        });
    }
}
