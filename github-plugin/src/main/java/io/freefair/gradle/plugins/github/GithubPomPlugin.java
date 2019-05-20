package io.freefair.gradle.plugins.github;

import io.freefair.gradle.plugins.github.internal.GithubService;
import io.freefair.gradle.plugins.github.internal.License;
import io.freefair.gradle.plugins.github.internal.Repo;
import io.freefair.gradle.plugins.github.internal.User;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.internal.publication.MavenPomInternal;
import org.gradle.api.publish.plugins.PublishingPlugin;

import java.time.ZoneId;
import java.util.Optional;

/**
 * @author Lars Grefer
 */
public class GithubPomPlugin implements Plugin<Project> {

    private GithubExtension githubExtension;

    private Project project;
    private GithubBasePlugin githubBasePlugin;
    private GithubService githubService;

    @Override
    public void apply(Project project) {
        this.project = project;

        githubBasePlugin = project.getRootProject().getPlugins().apply(GithubBasePlugin.class);
        githubExtension = githubBasePlugin.getGithubExtension();
        githubService = githubBasePlugin.getGithubClient().getGithubService();

        project.getPlugins().withType(PublishingPlugin.class, publishingPlugin -> {

            project.getExtensions().getByType(PublishingExtension.class).getPublications()
                    .withType(MavenPublication.class, this::configureMavenPublication);

        });
    }

    private void configureMavenPublication(MavenPublication mavenPublication) {
        mavenPublication.pom(pom -> {
            pom.getUrl().convention(getRepo().map(repo -> hasText(repo.getHomepage()).orElse(repo.getHtml_url())));
            pom.getDescription().convention(getRepo().flatMap(repo -> project.provider(() -> hasText(repo.getDescription()).orElse(project.getDescription()))));
            pom.getName().convention(getRepo().map(Repo::getName));
            pom.getInceptionYear().convention(getRepo().map(repo -> repo.getCreated_at().substring(0, 4)));

            pom.organization(organization -> {
                organization.getName().convention(getUser().map(user -> hasText(user.getName()).orElse(user.getLogin())));
                organization.getUrl().convention(getUser().map(user -> hasText(user.getBlog()).orElse(user.getHtml_url())));
            });

            project.afterEvaluate(rp -> {
                if (githubExtension.getTravis().getOrElse(false)) {
                    pom.ciManagement(ciManagement -> {
                        ciManagement.getSystem().convention("Travis CI");
                        ciManagement.getUrl().convention(githubExtension.getSlug().map(slug -> String.format("https://travis-ci.org/%s/", slug)));
                    });
                }

                if (getRepo().get().isHas_issues()) {
                    pom.issueManagement(issueManagement -> {
                        issueManagement.getSystem().convention("GitHub Issues");
                        issueManagement.getUrl().convention(githubExtension.getSlug().map(slug -> String.format("https://github.com/%s/issues", slug)));
                    });
                }

                if (getLicense().isPresent() && ((MavenPomInternal) pom).getLicenses().isEmpty()) {
                    pom.licenses(licences -> {
                        licences.license(licence -> {
                            licence.getName().convention(getLicense().map(License::getName));
                            licence.getUrl().convention(getLicense().map(License::getHtml_url));
                            licence.getComments().convention(getLicense().map(License::getDescription));
                        });
                    });
                }
            });

            pom.scm(scm -> {
                scm.getUrl().convention(getRepo().map(Repo::getHtml_url));
                scm.getConnection().convention(getRepo().map(repo -> "scm:git:" + repo.getClone_url()));
                scm.getDeveloperConnection().convention(scm.getConnection());
                scm.getTag().convention(githubExtension.getTag());
            });


        });
    }

    private Provider<Repo> getRepo() {
        return githubExtension.getSlug().flatMap(slug ->
                project.provider(() ->
                        githubService.getRepository(slug).execute().body()
                )
        );
    }

    private Provider<User> getUser() {
        return githubExtension.getOwner().flatMap(owner ->
                project.provider(() ->
                        githubService.getUser(owner).execute().body()
                )
        );
    }

    private Provider<License> getLicense() {
        return getRepo().flatMap(repo ->
                project.provider(() -> {
                    String url = Optional.ofNullable(repo.getLicense())
                            .map(License::getUrl)
                            .orElse(null);

                    if (url != null) {
                        return githubService.getLicense(url).execute().body();
                    }
                    else {
                        return null;
                    }
                })
        );
    }

    private Optional<String> hasText(String val) {
        return Optional.ofNullable(val).flatMap(s -> s.isEmpty() ? Optional.empty() : Optional.of(s));
    }
}
