package io.freefair.gradle.plugins.maven;

import io.freefair.gradle.plugins.base.AbstractPlugin;
import lombok.Getter;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.tasks.bundling.Jar;

/**
 * @author Lars Grefer
 */
@Getter
abstract class AbstractMavenJarPlugin extends AbstractPlugin {

    private Jar jarTask;

    @Override
    public void apply(Project project) {
        super.apply(project);
        jarTask = project.getTasks().create(getTaskName(), Jar.class);
        jarTask.setClassifier(getClassifier());
        jarTask.setGroup(BasePlugin.BUILD_GROUP);

        project.getPluginManager().apply(BasePlugin.class);
        project.getArtifacts().add(Dependency.ARCHIVES_CONFIGURATION, getJarTask());
    }

    protected abstract String getClassifier();

    @SuppressWarnings("WeakerAccess")
    protected String getTaskName() {
        return getClassifier() + "Jar";
    }
}
