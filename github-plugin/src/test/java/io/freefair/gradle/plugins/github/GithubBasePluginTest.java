package io.freefair.gradle.plugins.github;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class GithubBasePluginTest {

    private Project project;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder()
                .withProjectDir(new File("."))
                .build();
    }

    @Test
    void apply() {
        GithubBasePlugin basePlugin = project.getPlugins().apply(GithubBasePlugin.class);

        GithubExtension githubExtension = basePlugin.getGithubExtension();

        assertThat(githubExtension.getSlug().get()).isEqualTo("freefair/gradle-plugins");
    }
}
