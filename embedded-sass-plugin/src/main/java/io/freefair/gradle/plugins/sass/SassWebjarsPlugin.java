package io.freefair.gradle.plugins.sass;

import org.gradle.api.Incubating;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;

@Incubating
public class SassWebjarsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(SassBasePlugin.class);
        NamedDomainObjectProvider<Configuration> webjars = project.getConfigurations().register("webjars");

        project.getPlugins().withType(JavaPlugin.class, javaPlugin ->
                webjars.configure(wj -> {
                    Configuration compileClasspath = project.getConfigurations().named(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME).get();
                    wj.extendsFrom(compileClasspath);
                })
        );

        project.getTasks().withType(SassCompile.class)
                .configureEach(sassCompile -> sassCompile.getWebjars().from(webjars));
    }
}
