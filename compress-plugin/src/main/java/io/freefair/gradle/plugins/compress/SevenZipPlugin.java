package io.freefair.gradle.plugins.compress;

import io.freefair.gradle.plugins.compress.tasks.SevenZip;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Lars Grefer
 */
public class SevenZipPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getExtensions().getExtraProperties().set(
                SevenZip.class.getSimpleName(),
                SevenZip.class
        );
    }
}
