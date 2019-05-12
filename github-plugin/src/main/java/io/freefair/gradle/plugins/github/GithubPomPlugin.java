package io.freefair.gradle.plugins.github;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.plugins.PublishingPlugin;

/**
 * @author Lars Grefer
 */
public class GithubPomPlugin implements Plugin<Project> {

    private GithubExtension githubExtension;

    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;

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

            project.afterEvaluate(rp -> {
                if (githubExtension.getTravis().getOrElse(false)) {
                    pom.ciManagement(ciManagement -> {
                        ciManagement.getSystem().convention("Travis CI");
                        ciManagement.getUrl().convention(githubExtension.getSlug().map(slug -> String.format("https://travis-ci.org/%s/", slug)));
                    });
                }
            });

            pom.issueManagement(issueManagement -> {
                issueManagement.getSystem().convention("GitHub Issues");
                issueManagement.getUrl().convention(githubExtension.getSlug().map(slug -> String.format("https://github.com/%s/issues", slug)));
            });

            pom.scm(scm -> {
                scm.getUrl().convention(githubExtension.getSlug().map(slug -> String.format("https://github.com/%s/", slug)));
                scm.getConnection().convention(githubExtension.getSlug().map(slug -> String.format("scm:git:https://github.com/%s.git", slug)));
                scm.getDeveloperConnection().convention(githubExtension.getSlug().map(slug -> String.format("scm:git:git@github.com:%s.git", slug)));
                scm.getTag().convention(githubExtension.getTag());
            });
        });
    }
}
