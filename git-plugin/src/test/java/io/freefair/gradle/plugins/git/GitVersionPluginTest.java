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

    @Test
    void resolveTagVersion() {
        GitVersionPlugin plugin = new GitVersionPlugin();

        assertThat(plugin.resolveTagVersion("foo")).isEqualTo("foo");
        assertThat(plugin.resolveTagVersion("v1.3")).isEqualTo("1.3");
    }

    @Test
    void resolveBranchVersion() {
        GitVersionPlugin plugin = new GitVersionPlugin();

        assertThat(plugin.resolveBranchVersion("develop")).isEqualTo("develop-SNAPSHOT");
        assertThat(plugin.resolveBranchVersion("feature/foo")).isEqualTo("feature-foo-SNAPSHOT");
        assertThat(plugin.resolveBranchVersion("release-1.4")).isEqualTo("1.4-SNAPSHOT");
        assertThat(plugin.resolveBranchVersion("hotfix-1.4.3")).isEqualTo("1.4.3-SNAPSHOT");
    }
}
