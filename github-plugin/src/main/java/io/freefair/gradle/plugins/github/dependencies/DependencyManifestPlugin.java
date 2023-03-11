package io.freefair.gradle.plugins.github.dependencies;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.Usage;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskProvider;

public class DependencyManifestPlugin implements Plugin<Project> {

    @Getter
    private TaskProvider<DependencyManifestTask> manifestTaskProvider;

    @Override
    public void apply(Project project) {

        manifestTaskProvider = project.getTasks().register("githubDependenciesManifest", DependencyManifestTask.class, dependencyManifestTask -> {
            dependencyManifestTask.setGroup("github");
            dependencyManifestTask.getOutputFile().set(project.getLayout().getBuildDirectory().file("github/dependency-manifest.json"));

            Configuration classpath = project.getBuildscript().getConfigurations().getByName("classpath");
            dependencyManifestTask.getDevelopmentClasspaths().add(classpath.getIncoming().getResolutionResult().getRootComponent());
        });

        Configuration configuration = project.getConfigurations().create("githubDependenciesManifest");
        configuration.setCanBeResolved(false);
        configuration.setCanBeConsumed(true);
        configuration.getAttributes().attribute(Category.CATEGORY_ATTRIBUTE, project.getObjects().named(Category.class, Category.VERIFICATION));
        configuration.getAttributes().attribute(Bundling.BUNDLING_ATTRIBUTE, project.getObjects().named(Bundling.class, Bundling.EXTERNAL));
        configuration.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "github-dependency-manifest"));
        configuration.getOutgoing().artifact(manifestTaskProvider);

        project.getPlugins().withId("java", java -> {

            manifestTaskProvider.configure(dependencyManifestTask -> {
                Configuration compileClasspath = project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);
                dependencyManifestTask.getDevelopmentClasspaths().add(compileClasspath.getIncoming().getResolutionResult().getRootComponent());

                Configuration runtimeClasspath = project.getConfigurations().getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME);
                dependencyManifestTask.getRuntimeClasspaths().add(runtimeClasspath.getIncoming().getResolutionResult().getRootComponent());
            });
        });
    }
}
