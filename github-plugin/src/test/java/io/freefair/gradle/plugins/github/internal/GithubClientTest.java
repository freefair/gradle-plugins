package io.freefair.gradle.plugins.github.internal;

import io.freefair.gradle.plugins.github.GithubExtension;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Response;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class GithubClientTest {

    private GithubClient githubClient;

    @BeforeEach
    void setUp() {
        GithubExtension githubExtension = new GithubExtension(ProjectBuilder.builder().build().getObjects());

        githubExtension.getUsername().set("larsgrefer");
        githubExtension.getToken().set("ecddce86ef95fe71ffc31dc78b2466aa0f589a3a");

        githubClient = new GithubClient(githubExtension, null);
    }

    @Test
    void getGithubService() throws IOException {
        GithubService githubService = githubClient.getGithubService();

        Response<Repo> response = githubService.getRepository("freefair/gradle-plugins").execute();

        assertThat(response.isSuccessful()).isTrue();

        Repo repo = response.body();

        assertThat(repo.getHtml_url()).contains("freefair");
    }
}
