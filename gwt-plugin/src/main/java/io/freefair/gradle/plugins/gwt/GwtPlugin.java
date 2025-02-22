package io.freefair.gradle.plugins.gwt;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.WarPlugin;

/**
 * @author Lars Grefer
 */
public class GwtPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(GwtBasePlugin.class);

        project.getPlugins().withType(WarPlugin.class, warPlugin -> {
            project.getPlugins().apply(GwtWarPlugin.class);
        });

    }
}
