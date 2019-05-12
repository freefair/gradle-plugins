package io.freefair.gradle.plugins.github;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.plugins.PublishingPlugin;

public class GithubPomPlugin implements Plugin<Project> {

    private GithubExtension githubExtension;

    @Override
    public void apply(Project project) {
        githubExtension = project.getRootProject().getPlugins().apply(GithubBasePlugin.class).getGithubExtension();

        project.getPlugins().withType(PublishingPlugin.class, publishingPlugin -> {

            project.getExtensions().getByType(PublishingExtension.class).getPublications()
                    .withType(MavenPublication.class, this::configureMavenPublication);

        });
    }

    private void configureMavenPublication(MavenPublication mavenPublication) {
        mavenPublication.pom(pom -> {
            pom.getUrl().convention(githubExtension.getSlug().map(slug -> "https://github.com/" + slug));

            pom.organization(organization -> {
                organization.getName().convention(githubExtension.getOwner());
                organization.getUrl().convention(githubExtension.getOwner().map(owner -> "https://github.com/" + owner));
            });

            pom.issueManagement(issueManagement -> {
                issueManagement.getSystem().convention("GitHub Issues");
                issueManagement.getUrl().convention(githubExtension.getSlug().map(slug -> String.format("https://github.com/%s/issues", slug)));
            });

            pom.scm(scm -> {
                scm.getUrl().convention(githubExtension.getSlug().map(slug -> String.format("https://github.com/%s/", slug)));
                scm.getConnection().convention(githubExtension.getSlug().map(slug -> String.format("scm:git:https://github.com/%s.git", slug)));
                scm.getDeveloperConnection().convention(githubExtension.getSlug().map(slug -> String.format("scm:git:git@github.com:%s.git", slug)));
            });
        });
    }
}
