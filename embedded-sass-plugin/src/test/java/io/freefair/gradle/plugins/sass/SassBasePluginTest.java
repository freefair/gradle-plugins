package io.freefair.gradle.plugins.sass;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Baseline tests for SassBasePlugin.
 */
public class SassBasePluginTest {

    private Project project;

    @BeforeEach
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void pluginApplies() {
        project.getPlugins().apply(SassBasePlugin.class);

        assertThat(project.getPlugins().hasPlugin(SassBasePlugin.class)).isTrue();
    }

    @Test
    public void extensionIsRegistered() {
        project.getPlugins().apply(SassBasePlugin.class);

        SassExtension extension = project.getExtensions().getByType(SassExtension.class);

        assertThat(extension).isNotNull();
    }
}
