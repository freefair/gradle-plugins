package io.freefair.gradle.plugins.sass;

import org.gradle.api.Incubating;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskProvider;

@Incubating
public class SassWebjarsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(SassBasePlugin.class);
        Configuration webjars = project.getConfigurations().create("webjars");

        project.getPlugins().withType(JavaPlugin.class, javaPlugin ->
                webjars.extendsFrom(project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME))
        );

        TaskProvider<PrepareWebjars> prepareWebjarsTaskProvider = project.getTasks().register("prepareWebjars", PrepareWebjars.class, prepareWebjars -> {
            prepareWebjars.getWebjars().from(webjars);
            prepareWebjars.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("sass/webjars"));
        });

        project.getTasks().withType(SassCompile.class)
                .configureEach(sassCompile -> sassCompile.getIncludePaths().from(prepareWebjarsTaskProvider));
    }
}
