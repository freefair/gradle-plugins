package io.freefair.gradle.util;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GitUtilTest {

    Project project;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    void getSha() {
        String sha = GitUtil.getSha(project).get();

        assertThat(sha).hasSizeGreaterThan(12);
    }
}
