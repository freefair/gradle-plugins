package io.freefair.gradle.plugins.git;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GitVersionPluginTest {

    private Project project;

    @BeforeEach
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void apply() {
        project.getPlugins().apply(GitVersionPlugin.class);

        assertThat(project.getVersion()).isNotNull();
        assertThat(project.getVersion()).isInstanceOf(String.class);
    }
}
