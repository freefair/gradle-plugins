package io.freefair.gradle.plugins.maven.javadoc;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.Callable;

@Getter
public class LegacyAggregateJavadocPlugin implements Plugin<Project> {

    private TaskProvider<Javadoc> aggregateJavadoc;
    private ConfigurableFileCollection aggregateClasspath;

    @Override
    public void apply(Project project) {

        aggregateClasspath = project.files();

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
            aggregateJavadoc.setClasspath(aggregateClasspath);
        });

        project.allprojects(subproject -> {
            subproject.afterEvaluate(this::handleSubproject);
        });
    }

    private void handleSubproject(Project subproject) {
        subproject.getPlugins().withType(JavaPlugin.class, jp -> {

            AggregateJavadocClientPlugin clientPlugin = subproject.getPlugins().apply(AggregateJavadocClientPlugin.class);
            aggregateClasspath.from(clientPlugin.getJavadocClasspath());

            aggregateJavadoc.configure(aj -> {
                SourceSet main = subproject.getExtensions().getByType(JavaPluginExtension.class).getSourceSets().getByName("main");
                Javadoc javadoc = subproject.getTasks().named(main.getJavadocTaskName(), Javadoc.class).get();

                if (!javadoc.isEnabled()) {
                    aj.getLogger().info("Ignoring subproject {} as its javadoc task is disabled", subproject.getPath());
                    return;
                }

                aj.dependsOn(subproject.getTasks().findByName(JavaPlugin.CLASSES_TASK_NAME));
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
    }
}
