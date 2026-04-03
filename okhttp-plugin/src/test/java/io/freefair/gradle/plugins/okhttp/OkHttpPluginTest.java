package io.freefair.gradle.plugins.okhttp;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for OkHttpPlugin configuration and extension.
 */
public class OkHttpPluginTest {

    private Project project;

    @BeforeEach
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void pluginApplies() {
        project.getPlugins().apply(OkHttpPlugin.class);

        assertThat(project.getPlugins().hasPlugin(OkHttpPlugin.class)).isTrue();
    }

    @Test
    public void extensionIsRegistered() {
        project.getPlugins().apply(OkHttpPlugin.class);

        OkHttpExtension extension = project.getExtensions().getByType(OkHttpExtension.class);

        assertThat(extension).isNotNull();
        assertThat(extension.getCacheSize().get()).isEqualTo(10 * 1024 * 1024);
    }

    @Test
    public void extensionCanBeConfigured() {
        project.getPlugins().apply(OkHttpPlugin.class);

        OkHttpExtension extension = project.getExtensions().getByType(OkHttpExtension.class);
        extension.getCacheSize().set(5 * 1024 * 1024);

        assertThat(extension.getCacheSize().get()).isEqualTo(5 * 1024 * 1024);
    }

    @Test
    public void offlineModeEnablesForceCache() {
        project.getGradle().getStartParameter().setOffline(true);
        project.getPlugins().apply(OkHttpPlugin.class);

        OkHttpExtension extension = project.getExtensions().getByType(OkHttpExtension.class);

        assertThat(extension.getForceCache().get()).isTrue();
    }

    @Test
    public void onlineModeDoesNotForceCache() {
        project.getGradle().getStartParameter().setOffline(false);
        project.getPlugins().apply(OkHttpPlugin.class);

        OkHttpExtension extension = project.getExtensions().getByType(OkHttpExtension.class);

        assertThat(extension.getForceCache().get()).isFalse();
    }

    @Test
    public void forceCacheCanBeOverridden() {
        project.getPlugins().apply(OkHttpPlugin.class);

        OkHttpExtension extension = project.getExtensions().getByType(OkHttpExtension.class);
        extension.getForceCache().set(true);

        assertThat(extension.getForceCache().get()).isTrue();
    }
}
