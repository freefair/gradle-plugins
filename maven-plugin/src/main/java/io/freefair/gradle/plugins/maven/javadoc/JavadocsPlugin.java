package io.freefair.gradle.plugins.maven.javadoc;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Lars Grefer
 * @see JavadocLinksPlugin
 * @see JavadocJarPlugin
 */
public class JavadocsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(JavadocLinksPlugin.class);
        project.getPluginManager().apply(JavadocJarPlugin.class);
        project.getPluginManager().apply(JavadocUtf8Plugin.class);
    }
}
