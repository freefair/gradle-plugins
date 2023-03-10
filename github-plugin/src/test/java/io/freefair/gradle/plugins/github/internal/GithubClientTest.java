package io.freefair.gradle.plugins.github.internal;

import io.freefair.gradle.plugins.github.GithubExtension;
import okhttp3.OkHttpClient;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Response;

import java.io.IOException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class GithubClientTest {

    private GithubClient githubClient;

    @BeforeEach
    void setUp() {
        Project project = ProjectBuilder.builder().build();
        GithubExtension githubExtension = project.getExtensions().create("github", GithubExtension.class);

        githubClient = new GithubClient(githubExtension, new OkHttpClient());
    }

    @Test
    void getGithubService() throws IOException {
        GithubService githubService = githubClient.getGithubService();

        Response<Repo> response = githubService.getRepository("freefair/gradle-plugins").execute();

        String remaining = response.headers().get("X-RateLimit-Remaining");

        if (Objects.equals(remaining, "0")) {
            return;
        }

        assertThat(response.isSuccessful()).isTrue();

        Repo repo = response.body();

        assertThat(repo.getHtml_url()).contains("freefair");
    }
}
