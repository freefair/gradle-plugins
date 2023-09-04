package io.freefair.gradle.plugins.github;

import io.freefair.gradle.plugins.github.internal.GitUtils;
import io.freefair.gradle.plugins.github.internal.GithubClient;
import io.freefair.gradle.util.GitUtil;
import lombok.Getter;
import okhttp3.OkHttpClient;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;

/**
 * @author Lars Grefer
 */
public class GithubBasePlugin implements Plugin<Project> {

    @Getter
    private GithubExtension githubExtension;

    @Getter
    private GithubClient githubClient;

    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;

        if (project != project.getRootProject()) {
            project.getLogger().warn("This plugin should only be applied to the root project");
        }

        githubExtension = project.getExtensions().create("github", GithubExtension.class);

        githubExtension.getSlug().convention(project.provider(() -> GitUtils.findSlug(project)));

        githubExtension.getTravis().convention(project.provider(this::isTravis));
        githubExtension.getTag().convention(GitUtils.getTag(project));

        githubExtension.getUsername().convention(GitUtils.findGithubUsername(project));
        githubExtension.getToken().convention(GitUtils.findGithubToken(project));

        githubClient = new GithubClient(githubExtension, new OkHttpClient.Builder().build());
    }

    private boolean isTravis() {

        if (GitUtil.isTravisCi(project.getProviders())) {
            return true;
        }
        else if (GitUtil.isGithubActions(project.getProviders())) {
            return false;
        }

        if (new File(GitUtils.findWorkingDirectory(project).get(), ".travis.yml").isFile()) {
            return true;
        }

        return false;
    }
}
