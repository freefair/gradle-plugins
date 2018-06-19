package io.freefair.gradle.plugins.maven.javadoc;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.file.FileTreeInternal;
import org.gradle.api.internal.file.UnionFileCollection;
import org.gradle.api.internal.file.UnionFileTree;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class AggregateJavadocPlugin implements Plugin<Project> {

    private Javadoc aggregateJavadoc;

    @Override
    public void apply(Project project) {
        aggregateJavadoc = project.getTasks().create("aggregateJavadoc", Javadoc.class);
        aggregateJavadoc.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);

        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
            aggregateJavadoc.setDestinationDir(new File(
                    project.getConvention().getPlugin(JavaPluginConvention.class).getDocsDir(),
                    "aggregateJavadoc"
            ));
        });

        project.afterEvaluate(p -> {

            List<Javadoc> javadocTasks = project.getAllprojects().stream()
                    .peek(sub -> project.evaluationDependsOn(sub.getPath()))
                    .filter(sub -> sub.getPlugins().hasPlugin(JavaPlugin.class))
                    .map(sub -> (Javadoc) sub.getTasks().getByName(JavaPlugin.JAVADOC_TASK_NAME))
                    .collect(Collectors.toList());

            aggregateJavadoc.setClasspath(new UnionFileCollection(
                    javadocTasks.stream().map(Javadoc::getClasspath).collect(Collectors.toList())
            ));

            aggregateJavadoc.setSource(new UnionFileTree(
                    "aggregated sources",
                    javadocTasks.stream().map(Javadoc::getSource)
                            .map(FileTreeInternal.class::cast)
                            .collect(Collectors.toList())
            ));

            StandardJavadocDocletOptions aggregateJavadocOptions = (StandardJavadocDocletOptions) aggregateJavadoc.getOptions();

            javadocTasks.forEach(jd -> {
                StandardJavadocDocletOptions javadocOptions = (StandardJavadocDocletOptions) jd.getOptions();

                javadocOptions.getLinks().forEach(link -> {
                    if (!aggregateJavadocOptions.getLinks().contains(link)) {
                        aggregateJavadocOptions.links(link);
                    }
                });

                javadocOptions.getLinksOffline().forEach(offlineLink -> {
                    if (!aggregateJavadocOptions.getLinksOffline().contains(offlineLink)) {
                        aggregateJavadocOptions.getLinksOffline().add(offlineLink);
                    }
                });

                javadocOptions.getJFlags().forEach(jFlag -> {
                    if (!aggregateJavadocOptions.getJFlags().contains(jFlag))
                        aggregateJavadocOptions.jFlags(jFlag);
                });
            });
        });


    }
}
