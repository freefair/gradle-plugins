package io.freefair.gradle.plugins.compress;

import io.freefair.gradle.plugins.compress.tasks.Cpio;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Lars Grefer
 */
public class CpioPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getExtensions().getExtraProperties().set(
                Cpio.class.getSimpleName(),
                Cpio.class
        );
    }
}
