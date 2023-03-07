package io.freefair.gradle.plugins.maven.javadoc;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;

@Deprecated
public class AggregateJavadocJarPlugin implements Plugin<Project> {

    private TaskProvider<Jar> aggregateJavadocJar;

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(LegacyAggregateJavadocPlugin.class);

        project.getPlugins().withType(LegacyAggregateJavadocPlugin.class, aggregateJavadocPlugin -> {
            aggregateJavadocJar = project.getTasks().register("aggregateJavadocJar", Jar.class, aggregateJavadocJar -> {
                aggregateJavadocJar.from(aggregateJavadocPlugin.getAggregateJavadoc());
                aggregateJavadocJar.getArchiveClassifier().set("javadoc");
                aggregateJavadocJar.setGroup(BasePlugin.BUILD_GROUP);
            });

            project.getPlugins().apply(BasePlugin.class);
            project.getArtifacts().add(Dependency.ARCHIVES_CONFIGURATION, aggregateJavadocJar);

            project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
                aggregateJavadocJar.configure(aggregateJavadocJar -> {

                    aggregateJavadocJar.getArchiveClassifier().convention("aggregateJavadoc");
                    aggregateJavadocJar.getDestinationDirectory().set(
                            project.getExtensions().getByType(JavaPluginExtension.class).getDocsDir().dir("aggregateJavadoc")
                    );
                });
            });

        });
    }
}
