package io.freefair.gradle.plugins.maven.war;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class WarPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPlugins().apply(org.gradle.api.plugins.WarPlugin.class);
        project.getPlugins().apply(WarOverlayPlugin.class);
        project.getPlugins().apply(WarArchiveClassesPlugin.class);
        project.getPlugins().apply(WarAttachClassesPlugin.class);
    }
}
