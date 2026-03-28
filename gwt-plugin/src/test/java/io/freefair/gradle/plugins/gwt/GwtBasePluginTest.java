package io.freefair.gradle.plugins.gwt;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Baseline tests for GwtBasePlugin.
 */
public class GwtBasePluginTest {

    private Project project;

    @BeforeEach
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void pluginApplies() {
        project.getPlugins().apply(GwtBasePlugin.class);

        assertThat(project.getPlugins().hasPlugin(GwtBasePlugin.class)).isTrue();
    }

    @Test
    public void extensionIsRegistered() {
        project.getPlugins().apply(GwtBasePlugin.class);

        GwtExtension extension = project.getExtensions().getByType(GwtExtension.class);

        assertThat(extension).isNotNull();
    }

    @Test
    public void configurationsAreCreated() {
        project.getPlugins().apply(GwtBasePlugin.class);

        Configuration gwtDev = project.getConfigurations().findByName("gwtDev");
        Configuration gwtClasspath = project.getConfigurations().findByName("gwtClasspath");
        Configuration gwtSources = project.getConfigurations().findByName("gwtSources");

        assertThat(gwtDev).isNotNull();
        assertThat(gwtClasspath).isNotNull();
        assertThat(gwtSources).isNotNull();
    }

    @Test
    public void tasksAreRegistered() {
        project.getPlugins().apply(GwtBasePlugin.class);

        assertThat(project.getTasks().findByName("gwtCompile")).isNotNull();
        assertThat(project.getTasks().findByName("gwtDevMode")).isNotNull();
        assertThat(project.getTasks().findByName("gwtCodeServer")).isNotNull();
    }
}
