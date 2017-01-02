package io.freefair.gradle.plugins.javadoc;

import io.freefair.gradle.plugins.AbstractGroupingPlugin;
import io.freefair.gradle.plugins.JavadocJarPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Arrays;
import java.util.List;

/**
 * @author Lars Grefer
 * @see JavadocLinksPlugin
 * @see JavadocIoPlugin
 * @see JavadocJarPlugin
 */
@SuppressWarnings("unused")
public class JavadocPlugin extends AbstractGroupingPlugin {

    @Override
    protected List<Class<? extends Plugin<Project>>> getPlugins() {
        return Arrays.asList(
                (Class<? extends Plugin<Project>>) JavadocLinksPlugin.class,
                JavadocIoPlugin.class,
                JavadocJarPlugin.class
        );
    }
}
