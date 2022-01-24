package io.freefair.gradle.plugins.github;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class GithubBasePluginTest {

    private Project project;

    @TempDir
    private File gradleUserHome;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder()
                .withProjectDir(new File("."))
                .withGradleUserHomeDir(gradleUserHome)
                .build();
    }

    @Test
    void apply() {
        GithubBasePlugin basePlugin = project.getPlugins().apply(GithubBasePlugin.class);

        GithubExtension githubExtension = basePlugin.getGithubExtension();

        assertThat(githubExtension.getSlug().get()).isEqualTo("freefair/gradle-plugins");
    }
}
