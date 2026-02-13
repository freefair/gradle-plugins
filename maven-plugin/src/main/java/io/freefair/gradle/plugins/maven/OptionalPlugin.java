package io.freefair.gradle.plugins.maven;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;

/**
 * Plugin that provides an {@code optional} configuration for optional dependencies.
 * <p>
 * Creates an {@code optional} configuration that extends {@code compileOnly},
 * {@code testCompileOnly}, and {@code testRuntimeOnly}. This allows declaring
 * dependencies that are:
 * <ul>
 *   <li>Available at compile time but not included in the runtime classpath</li>
 *   <li>Available for test compilation and execution</li>
 *   <li>Similar to Maven's {@code <optional>true</optional>} dependency scope</li>
 * </ul>
 */
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
