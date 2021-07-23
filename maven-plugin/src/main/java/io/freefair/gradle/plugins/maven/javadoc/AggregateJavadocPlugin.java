package io.freefair.gradle.plugins.maven.javadoc;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

@Getter
public class AggregateJavadocPlugin implements Plugin<Project> {

    private TaskProvider<Javadoc> aggregateJavadoc;

    @Override
    public void apply(Project project) {

        Configuration classpathConfiguration = project.getConfigurations().create("aggregateJavadocClasspath");

        aggregateJavadoc = project.getTasks().register("aggregateJavadoc", Javadoc.class, aggregateJavadoc -> {
            aggregateJavadoc.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
            aggregateJavadoc.getConventionMapping().map("destinationDir", new Callable<Object>() {
                @Override
                public Object call() {
                    File docsDir = Optional.ofNullable(project.getExtensions().findByType(JavaPluginExtension.class))
                            .map(JavaPluginExtension::getDocsDir)
                            .map(directoryProperty -> directoryProperty.get().getAsFile())
                            .orElse(new File(project.getBuildDir(), "docs"));
                    return new File(docsDir, "aggregateJavadoc");
                }
            });
            aggregateJavadoc.setClasspath(classpathConfiguration);
        });

        project.allprojects(subproject -> {
            subproject.getPlugins().withType(JavaPlugin.class, jp -> {

                SourceSet main = subproject.getExtensions().getByType(JavaPluginExtension.class).getSourceSets().getByName("main");
                Configuration compileClasspath = subproject.getConfigurations().getByName(main.getCompileClasspathConfigurationName());
                Configuration javadocClasspathProvider = subproject.getConfigurations().maybeCreate("javadocClasspathProvider");
                javadocClasspathProvider.extendsFrom(compileClasspath);

                Map<String, String> dep = new HashMap<>();
                dep.put("path", subproject.getPath());
                dep.put("configuration", javadocClasspathProvider.getName());
                classpathConfiguration.getDependencies().add(project.getDependencies().project(dep));

                aggregateJavadoc.configure(aj -> {
                    Javadoc javadoc = subproject.getTasks().named(main.getJavadocTaskName(), Javadoc.class).get();

                    aj.source(javadoc.getSource());

                    StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) javadoc.getOptions();
                    StandardJavadocDocletOptions aggregateOptions = (StandardJavadocDocletOptions) aj.getOptions();

                    options.getLinks().forEach(link -> {
                        if (!aggregateOptions.getLinks().contains(link)) {
                            aggregateOptions.getLinks().add(link);
                        }
                    });
                    options.getLinksOffline().forEach(link -> {
                        if (!aggregateOptions.getLinksOffline().contains(link)) {
                            aggregateOptions.getLinksOffline().add(link);
                        }
                    });
                    options.getJFlags().forEach(jFlag -> {
                        if (!aggregateOptions.getJFlags().contains(jFlag)) {
                            aggregateOptions.getJFlags().add(jFlag);
                        }
                    });
                });
            });
        });
    }
}
