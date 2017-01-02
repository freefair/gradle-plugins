package io.freefair.gradle.plugins.maven;

import io.freefair.gradle.plugins.base.AbstractGroupingPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Arrays;
import java.util.List;

/**
 * @author Lars Grefer
 */
@SuppressWarnings("unused")
public class MavenJarsPlugin extends AbstractGroupingPlugin {

    @Override
    protected List<Class<? extends Plugin<Project>>> getPlugins() {
        return Arrays.asList(
                (Class<? extends Plugin<Project>>) SourcesJarPlugin.class,
                JavadocJarPlugin.class
        );
    }
}
