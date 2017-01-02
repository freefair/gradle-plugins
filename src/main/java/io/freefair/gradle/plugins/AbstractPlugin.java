package io.freefair.gradle.plugins;

import lombok.Getter;
import org.gradle.api.Action;
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

        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                afterEvaluate(project);
            }
        });
    }

    protected void afterEvaluate(Project project) {

    }
}
