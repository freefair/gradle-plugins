package io.freefair.gradle.plugins.compress;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Lars Grefer
 */
public class CompressPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPlugins().apply(CompressTreePlugin.class);
        project.getPlugins().apply(ArPlugin.class);
        project.getPlugins().apply(CpioPlugin.class);
        project.getPlugins().apply(SevenZipPlugin.class);
    }
}
