package io.freefair.gradle.plugins.compress;

import io.freefair.gradle.plugins.compress.tasks.Ar;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Lars Grefer
 */
public class ArPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getExtensions().getExtraProperties().set(
                Ar.class.getSimpleName(),
                Ar.class
        );
    }
}
