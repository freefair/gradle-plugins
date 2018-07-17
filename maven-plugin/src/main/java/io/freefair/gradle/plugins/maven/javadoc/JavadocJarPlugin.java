package io.freefair.gradle.plugins.maven.javadoc;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;

import java.io.File;

@Getter
public class JavadocJarPlugin implements Plugin<Project> {

    private Jar javadocJar;
    private Jar aggregateJavadocJar;

    @Override
    public void apply(Project project) {
        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
            Javadoc javadoc = (Javadoc) project.getTasks().getByName(JavaPlugin.JAVADOC_TASK_NAME);

            javadocJar = project.getTasks().create("javadocJar", Jar.class);
            javadocJar.from(javadoc);
            javadocJar.setClassifier("javadoc");
            javadocJar.setDescription("Assembles a jar archive containing the javadocs.");
            javadocJar.setGroup(BasePlugin.BUILD_GROUP);

            project.getArtifacts().add(Dependency.ARCHIVES_CONFIGURATION, javadocJar);
        });

        project.getPlugins().withType(AggregateJavadocPlugin.class, aggregateJavadocPlugin -> {
            Javadoc aggregateJavadoc = aggregateJavadocPlugin.getAggregateJavadoc();

            aggregateJavadocJar = project.getTasks().create("aggregateJavadocJar", Jar.class);
            aggregateJavadocJar.from(aggregateJavadoc);
            aggregateJavadocJar.setClassifier("javadoc");
            aggregateJavadocJar.setGroup(BasePlugin.BUILD_GROUP);

            project.getPlugins().apply(BasePlugin.class);
            project.getArtifacts().add(Dependency.ARCHIVES_CONFIGURATION, aggregateJavadocJar);

            project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
                aggregateJavadocJar.setClassifier("aggregateJavadoc");
                aggregateJavadocJar.setDestinationDir(new File(
                        project.getConvention().getPlugin(JavaPluginConvention.class).getDocsDir(),
                        "aggregateJavadoc"
                ));
            });

        });
    }
}
