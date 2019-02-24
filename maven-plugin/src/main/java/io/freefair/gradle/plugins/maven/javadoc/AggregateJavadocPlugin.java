package io.freefair.gradle.plugins.maven.javadoc;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

@Getter
public class AggregateJavadocPlugin implements Plugin<Project> {

    private TaskProvider<Javadoc> aggregateJavadoc;

    @Override
    public void apply(Project project) {
        aggregateJavadoc = project.getTasks().register("aggregateJavadoc", Javadoc.class, aggregateJavadoc ->
                aggregateJavadoc.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP)
        );

        project.allprojects(p ->
                p.getPlugins().withType(JavaPlugin.class, jp ->
                        aggregateJavadoc.configure(aj -> {
                            Javadoc javadoc = (Javadoc) p.getTasks().getByName(JavaPlugin.JAVADOC_TASK_NAME);

                            aj.setClasspath(aj.getClasspath().plus(javadoc.getClasspath()));
                            aj.setSource(aj.getSource().plus(javadoc.getSource()));

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
                        })
                )
        );

        project.afterEvaluate(p -> aggregateJavadoc.configure(aggregateJavadoc -> {
            if (aggregateJavadoc.getDestinationDir() == null) {
                File docsDir = Optional.ofNullable(project.getConvention().findPlugin(JavaPluginConvention.class))
                        .map(JavaPluginConvention::getDocsDir)
                        .orElse(new File(project.getBuildDir(), "docs"));

                aggregateJavadoc.setDestinationDir(new File(docsDir, "aggregateJavadoc"));
            }
        }));
    }
}
