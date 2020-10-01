package io.freefair.gradle.plugins.github;

import io.freefair.gradle.plugins.github.internal.GitUtils;
import io.freefair.gradle.plugins.github.internal.GithubClient;
import io.freefair.gradle.plugins.okhttp.OkHttpPlugin;
import lombok.Getter;
import okhttp3.OkHttpClient;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.provider.Provider;
import org.gradle.initialization.layout.ProjectCacheDir;

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
        githubExtension.getTag().convention(project.provider(() -> GitUtils.getTag(project)));

        String github_actor = System.getenv("GITHUB_ACTOR");
        if (github_actor != null) {
            githubExtension.getUsername().convention(System.getenv("GITHUB_ACTOR"));
        }
        String github_token = System.getenv("GITHUB_TOKEN");
        if (github_token != null) {
            githubExtension.getToken().convention(System.getenv("GITHUB_TOKEN"));
        }

        OkHttpPlugin okHttpPlugin = project.getPlugins().apply(OkHttpPlugin.class);

        githubClient = new GithubClient(githubExtension, okHttpPlugin.getOkHttpClient());
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
