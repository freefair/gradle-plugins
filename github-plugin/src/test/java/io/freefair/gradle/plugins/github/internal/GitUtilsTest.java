package io.freefair.gradle.plugins.github.internal;

import io.freefair.gradle.plugins.github.internal.GitUtils;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;


class GitUtilsTest {

    private Project project;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder()
                .withProjectDir(new File("."))
                .build();

    }

    @Test
    void findSlug() {
        assertThat(GitUtils.findSlug(project))
                .isEqualTo("freefair/gradle-plugins");
    }

    @Test
    void getRemoteUrl() {
        assertThat(GitUtils.getRemoteUrl(project, "origin"))
                .contains("freefair/gradle-plugins");
    }

    @Test
    void findWorkingDirectory() {
        assertThat(GitUtils.findWorkingDirectory(project)).isNotNull();
    }

    @Test
    void getTag() {
        assertThat(GitUtils.getTag(project)).isNotNull();
    }
}
