package io.freefair.gradle.plugins.github;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GithubBasePlugin implements Plugin<Project> {

    @Getter
    private GithubExtension githubExtension;

    @Override
    public void apply(Project project) {
        githubExtension = project.getExtensions().create("github", GithubExtension.class);

        githubExtension.getSlug().convention(project.provider(() -> GitUtils.findSlug(project)));
    }


}
