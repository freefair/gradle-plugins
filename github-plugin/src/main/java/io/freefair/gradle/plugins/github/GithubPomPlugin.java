package io.freefair.gradle.plugins.github;

import io.freefair.gradle.plugins.github.internal.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.internal.publication.MavenPomInternal;
import org.gradle.api.publish.plugins.PublishingPlugin;

import java.io.IOException;

/**
 * @author Lars Grefer
 */
public class GithubPomPlugin implements Plugin<Project> {

    private GithubExtension githubExtension;

    private Project project;
    private GithubBasePlugin githubBasePlugin;
    private GithubService githubService;
    private String slug;
    private Repo repo;
    private User user;
    private License ghLicense;

    @Override
    public void apply(Project project) {
        this.project = project;

        githubBasePlugin = project.getRootProject().getPlugins().apply(GithubBasePlugin.class);
        githubExtension = githubBasePlugin.getGithubExtension();
        githubService = githubBasePlugin.getGithubClient().getGithubService();

        project.afterEvaluate(p -> {

            slug = githubExtension.getSlug().get();

            try {
                repo = githubService.getRepository(slug).execute().body();
                user = githubService.getUser(githubExtension.getOwner().get()).execute().body();
                if (repo.getLicense() != null) {
                    ghLicense = githubService.getLicense(repo.getLicense().getUrl()).execute().body();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            project.getPlugins().withType(PublishingPlugin.class, publishingPlugin -> {
                project.getExtensions().getByType(PublishingExtension.class).getPublications()
                        .withType(MavenPublication.class, this::configureMavenPublication);

            });
        });
    }

    private void configureMavenPublication(MavenPublication mavenPublication) {
        mavenPublication.pom(pom -> {

            if (hasText(repo.getHomepage())) {
                pom.getUrl().convention(repo.getHomepage());
            }
            else {
                pom.getUrl().convention(repo.getHtml_url());
            }

            if (hasText(repo.getDescription())) {
                pom.getDescription().convention(repo.getDescription());
            }
            else {
                pom.getDescription().convention(project.getDescription());
            }

            pom.getName().convention(project.getName());
            pom.getInceptionYear().convention(repo.getCreated_at().substring(0, 4));

            pom.organization(organization -> {
                if (hasText(user.getName())) {
                    organization.getName().convention(user.getName());
                }
                else {
                    organization.getName().convention(user.getLogin());
                }

                if (hasText(user.getBlog())) {
                    organization.getUrl().convention(user.getBlog());
                }
                else {
                    organization.getUrl().convention(user.getHtml_url());
                }
            });

            if (githubExtension.getTravis().getOrElse(false)) {
                pom.ciManagement(ciManagement -> {
                    ciManagement.getSystem().convention("Travis CI");
                    ciManagement.getUrl().convention(String.format("https://travis-ci.org/%s/", slug));
                });
            }
            else if (GitUtils.currentlyRunningOnGithubActions()) {
                pom.ciManagement(ciManagement -> {
                    ciManagement.getSystem().convention("GitHub Actions");
                    ciManagement.getUrl().convention(String.format("https://github.com/%s/actions", slug));
                });
            }

            if (repo.isHas_issues()) {
                pom.issueManagement(issueManagement -> {
                    issueManagement.getSystem().convention("GitHub Issues");
                    issueManagement.getUrl().convention(String.format("https://github.com/%s/issues", slug));
                });
            }

            if (ghLicense != null && ((MavenPomInternal) pom).getLicenses().isEmpty()) {
                pom.licenses(licences -> {
                    licences.license(licence -> {
                        licence.getName().convention(ghLicense.getName());
                        licence.getUrl().convention(ghLicense.getHtml_url());
                        licence.getComments().convention(ghLicense.getDescription());
                    });
                });
            }

            pom.scm(scm -> {
                scm.getUrl().convention(repo.getHtml_url());
                scm.getConnection().convention("scm:git:" + repo.getClone_url());
                scm.getDeveloperConnection().convention("scm:git:" + repo.getSsh_url());
                scm.getTag().convention(githubExtension.getTag());
            });

        });
    }

    private boolean hasText(String text) {
        return text != null && !text.isEmpty();
    }

}
