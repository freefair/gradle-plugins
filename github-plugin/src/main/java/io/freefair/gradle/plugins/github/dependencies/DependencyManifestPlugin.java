package io.freefair.gradle.plugins.github.dependencies;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskProvider;

public class DependencyManifestPlugin implements Plugin<Project> {

    @Getter
    private TaskProvider<DependencyManifestTask> manifestTaskProvider;

    @Override
    public void apply(Project project) {
        project.getPlugins().withId("java", java -> {

            manifestTaskProvider = project.getTasks().register("githubDependenciesManifest", DependencyManifestTask.class, dependencyManifestTask -> {
                dependencyManifestTask.setGroup("github");
                dependencyManifestTask.getOutputFile().set(project.getLayout().getBuildDirectory().file("github/dependency-manifest.json"));

                Configuration compileClasspath = project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);
                dependencyManifestTask.getCompileClasspath().set(compileClasspath.getIncoming().getResolutionResult().getRootComponent());

                Configuration runtimeClasspath = project.getConfigurations().getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME);
                dependencyManifestTask.getRuntimeClasspath().set(runtimeClasspath.getIncoming().getResolutionResult().getRootComponent());
            });

        });
    }
}
