package io.freefair.gradle.plugins.github;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

/**
 * @author Lars Grefer
 */
public class GithubPackageRegistryMavenPublishPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {

        project.getPlugins().apply(MavenPublishPlugin.class);
        project.getPlugins().apply(GithubPomPlugin.class);

        GithubExtension githubExtension = project.getRootProject().getExtensions().getByType(GithubExtension.class);

        project.afterEvaluate(p -> p.getExtensions().getByType(PublishingExtension.class)
                .getRepositories()
                .maven(githubRepo -> {
                    String owner = githubExtension.getOwner().get();
                    githubRepo.setName("GitHub " + owner + " Maven Packages");
                    githubRepo.setUrl("https://maven.pkg.github.com/" + owner);

                    if (githubExtension.getUsername().isPresent() && githubExtension.getToken().isPresent()) {
                        githubRepo.credentials(passwordCredentials -> {
                            passwordCredentials.setUsername(githubExtension.getUsername().get());
                            passwordCredentials.setPassword(githubExtension.getToken().get());
                        });
                    }

                }));


    }
}
