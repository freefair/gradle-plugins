package io.freefair.gradle.plugins.maven.javadoc;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.Callable;

@Getter
public class AggregateJavadocPlugin implements Plugin<Project> {

    private TaskProvider<Javadoc> aggregateJavadoc;

    @Override
    public void apply(Project project) {

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
        });

        project.allprojects(p ->
                p.getPlugins().withType(JavaPlugin.class, jp ->
                        aggregateJavadoc.configure(aj -> {
                            TaskProvider<Javadoc> javadoc = p.getTasks().named(JavaPlugin.JAVADOC_TASK_NAME, Javadoc.class);

                            aj.source(javadoc.map(Javadoc::getSource));

                            if (aj.getClasspath() instanceof ConfigurableFileCollection) {
                                ((ConfigurableFileCollection) aj.getClasspath()).from(javadoc.map(Javadoc::getClasspath));
                            }
                            else {
                                ConfigurableFileCollection classpath = project.files();
                                classpath.from(aj.getClasspath());
                                classpath.from(javadoc.map(Javadoc::getClasspath));
                                aj.setClasspath(classpath);
                            }

                            StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) javadoc.get().getOptions();
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
                        })
                )
        );
    }
}
