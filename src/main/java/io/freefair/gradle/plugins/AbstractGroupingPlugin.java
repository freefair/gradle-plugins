package io.freefair.gradle.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.List;

/**
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
