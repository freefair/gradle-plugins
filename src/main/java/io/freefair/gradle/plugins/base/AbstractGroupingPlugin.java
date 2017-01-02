package io.freefair.gradle.plugins.base;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.List;

/**
 * Base class for {@link org.gradle.api.Plugin plugins} which are just {@link org.gradle.api.plugins.PluginManager#apply(Class) applying} other plugins.
 *
 * @author Lars Grefer
 */
public abstract class AbstractGroupingPlugin extends AbstractPlugin {

    @Override
    public void apply(Project project) {
        super.apply(project);

        for (Class<? extends Plugin<Project>> pluginClass : getPlugins()) {
            project.getPluginManager().apply(pluginClass);
        }
    }

    protected abstract List<Class<? extends Plugin<Project>>> getPlugins();
}
