package io.freefair.gradle.plugins.jsass;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskProvider;

@Deprecated
public class JSassWebjarsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JSassBasePlugin.class);
        Configuration webjars = project.getConfigurations().create("webjars");

        project.getPlugins().withType(JavaPlugin.class, javaPlugin ->
                webjars.extendsFrom(project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME))
        );

        TaskProvider<PrepareWebjars> prepareWebjarsTaskProvider = project.getTasks().register("prepareWebjars", PrepareWebjars.class, prepareWebjars -> {
            prepareWebjars.getWebjars().from(webjars);
            prepareWebjars.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("jsass/webjars"));
        });

        project.getTasks().withType(SassCompile.class)
                .configureEach(sassCompile -> sassCompile.getIncludePaths().from(prepareWebjarsTaskProvider));
    }
}
