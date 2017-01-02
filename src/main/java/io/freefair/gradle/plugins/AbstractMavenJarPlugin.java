package io.freefair.gradle.plugins;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.bundling.Jar;

/**
 * @author Lars Grefer
 */
@Getter
@RequiredArgsConstructor
abstract class AbstractMavenJarPlugin implements Plugin<Project> {

    final String taskName;
    final String classifier;

    private Jar jarTask;

    @Override
    public void apply(Project project) {
        jarTask = project.getTasks().create(taskName, Jar.class, new Action<Jar>() {
            @Override
            public void execute(Jar jar) {
                jar.setClassifier(classifier);
            }
        });

        project.getArtifacts().add(Dependency.ARCHIVES_CONFIGURATION, getJarTask());
    }
}
