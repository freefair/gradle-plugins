package io.freefair.gradle.plugins.github;

import io.freefair.gradle.plugins.github.internal.GitUtils;
import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;
import java.io.UnsupportedEncodingException;

public class GithubBasePlugin implements Plugin<Project> {

    @Getter
    private GithubExtension githubExtension;

    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;

        githubExtension = project.getExtensions().create("github", GithubExtension.class);

        githubExtension.getSlug().convention(project.provider(() -> GitUtils.findSlug(project)));

        githubExtension.getTravis().convention(project.provider(this::isTravis));
        githubExtension.getTag().convention(project.provider(() -> GitUtils.getTag(project)));
    }

    private boolean isTravis() {

        String travisEnv = System.getenv("TRAVIS");
        if (travisEnv != null) {
            return travisEnv.trim().equalsIgnoreCase("true");
        }

        if (new File(GitUtils.findWorkingDirectory(project), ".travis.yml").isFile()) {
            return true;
        }

        return false;
    }


}
