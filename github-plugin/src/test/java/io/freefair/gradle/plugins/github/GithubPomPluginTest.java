package io.freefair.gradle.plugins.github;

import io.freefair.gradle.plugins.maven.MavenPublishJavaPlugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GithubPomPluginTest {

    private Project project;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    void apply() {
        project.getPlugins().apply(GithubPomPlugin.class);
        project.getPlugins().apply(MavenPublishJavaPlugin.class);
    }

}
