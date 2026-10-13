package io.freefair.gradle.plugins.compress.web;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.jvm.tasks.ProcessResources;

public class CompressJavaResourcesPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {

        project.getPlugins().apply(JavaPlugin.class);

        for (SourceSet sourceSet : project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets()) {

            TaskProvider<ProcessResources> processResourcesTask = project.getTasks().named(sourceSet.getProcessResourcesTaskName(), ProcessResources.class);

            String taskName = sourceSet.getTaskName("compress", "resources");
            TaskProvider<CompressResourcesTask> resourceCompressionTask = project.getTasks().register(taskName, CompressResourcesTask.class, compressResourcesTask -> {
                compressResourcesTask.source(sourceSet.getResources());
                compressResourcesTask.getOutputDir().set(project.getLayout().getBuildDirectory().dir("compressed-" + sourceSet.getName() + "-resources"));
            });

            processResourcesTask.configure(processResources -> processResources.from(resourceCompressionTask));

        }

    }
}
