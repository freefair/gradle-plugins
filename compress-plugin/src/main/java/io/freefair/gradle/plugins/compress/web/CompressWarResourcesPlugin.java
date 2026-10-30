package io.freefair.gradle.plugins.compress.web;

import io.freefair.gradle.plugins.compress.brotli4j.Brotli4jBasePlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.War;

public class CompressWarResourcesPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getPlugins().apply(WarPlugin.class);
        project.getPlugins().apply(Brotli4jBasePlugin.class);

        TaskProvider<War> warTask = project.getTasks().named("war", War.class);

        TaskProvider<CompressResourcesTask> compressWarResources = project.getTasks().register("compressWebappResources", CompressResourcesTask.class, compressResourcesTask -> {
            compressResourcesTask.source(warTask.flatMap(War::getWebAppDirectory));

            compressResourcesTask.getOutputDir().set(project.getLayout().getBuildDirectory().dir("compressed-war-resources"));
        });

        warTask.configure(war -> war.from(compressWarResources));


    }
}
