package io.freefair.gradle.plugins.javadoc;

import io.freefair.gradle.plugins.base.AbstractPlugin;
import io.freefair.gradle.plugins.maven.JavadocJarPlugin;
import org.gradle.api.Project;

/**
 * @author Lars Grefer
 * @see JavadocLinksPlugin
 * @see JavadocIoPlugin
 * @see JavadocJarPlugin
 */
@SuppressWarnings("unused")
public class JavadocPlugin extends AbstractPlugin {

    @Override
    public void apply(Project project) {
        super.apply(project);
        project.getPluginManager().apply(JavadocLinksPlugin.class);
        project.getPluginManager().apply(JavadocIoPlugin.class);
        project.getPluginManager().apply(JavadocJarPlugin.class);
    }
}
