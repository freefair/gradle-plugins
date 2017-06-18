package io.freefair.gradle.plugins.base;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Lars Grefer
 */
@Getter
public abstract class AbstractPlugin implements Plugin<Project> {

    protected Project project;

    @Override
    public void apply(Project project) {
        this.project = project;
    }
}
