package io.freefair.gradle.plugins.maven.javadoc;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class AggregateJavadocJarPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPlugins().apply(AggregateJavadocPlugin.class);
        project.getPlugins().apply(JavadocJarPlugin.class);
    }
}
