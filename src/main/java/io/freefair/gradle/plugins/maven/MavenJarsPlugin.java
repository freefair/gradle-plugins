package io.freefair.gradle.plugins.maven;

import io.freefair.gradle.plugins.base.AbstractPlugin;
import org.gradle.api.Project;

/**
 * @author Lars Grefer
 */
@SuppressWarnings("unused")
public class MavenJarsPlugin extends AbstractPlugin {

    @Override
    public void apply(Project project) {
        super.apply(project);
        project.getPluginManager().apply(SourcesJarPlugin.class);
        project.getPluginManager().apply(JavadocJarPlugin.class);
    }
}
