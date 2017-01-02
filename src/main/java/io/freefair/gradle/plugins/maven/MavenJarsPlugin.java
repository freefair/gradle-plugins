package io.freefair.gradle.plugins.maven;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Lars Grefer
 */
@SuppressWarnings("unused")
public class MavenJarsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(SourcesJarPlugin.class);
        project.getPluginManager().apply(JavadocJarPlugin.class);
    }
}
