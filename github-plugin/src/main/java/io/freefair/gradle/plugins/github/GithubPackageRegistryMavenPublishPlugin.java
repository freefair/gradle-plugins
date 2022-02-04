package io.freefair.gradle.plugins.github;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

/**
 * @author Lars Grefer
 * @see https://docs.github.com/en/free-pro-team@latest/packages/using-github-packages-with-your-projects-ecosystem/configuring-gradle-for-use-with-github-packages
 */
public class GithubPackageRegistryMavenPublishPlugin implements Plugin<Project> {

    private GithubExtension githubExtension;

    @Getter
    private MavenArtifactRepository githubRepository;

    @Override
    public void apply(Project project) {

        project.getPlugins().apply(MavenPublishPlugin.class);
        project.getPlugins().apply(GithubPomPlugin.class);

        githubExtension = project.getRootProject().getExtensions().getByType(GithubExtension.class);

        project.afterEvaluate(this::registerGitHubPackagesRepository);
    }

    private void registerGitHubPackagesRepository(Project project) {
        PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
        this.githubRepository = publishing
                .getRepositories()
                .maven(githubRepo -> {
                    githubRepo.setName("GitHubPackages");
                    githubRepo.setUrl("https://maven.pkg.github.com/" + githubExtension.getSlug().get());

                    if (githubExtension.getUsername().isPresent() && githubExtension.getToken().isPresent()) {
                        githubRepo.credentials(passwordCredentials -> {
                            passwordCredentials.setUsername(githubExtension.getUsername().get());
                            passwordCredentials.setPassword(githubExtension.getToken().get());
                        });
                    }

                });
    }
}
