package io.freefair.gradle.plugins.github.internal;

import io.freefair.gradle.plugins.github.GithubExtension;
import okhttp3.OkHttpClient;
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

        githubClient = new GithubClient(githubExtension, new OkHttpClient());
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
