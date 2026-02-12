package io.freefair.gradle.plugins.jacoco;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.tasks.JacocoReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Baseline tests for AggregateJacocoReportPlugin.
 */
public class AggregateJacocoReportPluginTest {

    private Project rootProject;

    @BeforeEach
    public void setUp() {
        rootProject = ProjectBuilder.builder().build();
    }

    @Test
    public void pluginApplies() {
        rootProject.getPlugins().apply(AggregateJacocoReportPlugin.class);

        assertThat(rootProject.getPlugins().hasPlugin(AggregateJacocoReportPlugin.class)).isTrue();
        assertThat(rootProject.getPlugins().hasPlugin(JacocoPlugin.class)).isTrue();
    }

    @Test
    public void aggregateTaskIsRegistered() {
        rootProject.getPlugins().apply(AggregateJacocoReportPlugin.class);

        assertThat(rootProject.getTasks().findByName("aggregateJacocoReport")).isNotNull();
        assertThat(rootProject.getTasks().findByName("aggregateJacocoReport"))
                .isInstanceOf(JacocoReport.class);
    }

    @Test
    public void aggregateTaskWithSubprojects() {
        // Create subproject
        Project subproject = ProjectBuilder.builder()
                .withName("subproject")
                .withParent(rootProject)
                .build();

        rootProject.getPlugins().apply(AggregateJacocoReportPlugin.class);
        subproject.getPlugins().apply(JavaPlugin.class);

        JacocoReport aggregateTask = (JacocoReport) rootProject.getTasks().getByName("aggregateJacocoReport");

        assertThat(aggregateTask).isNotNull();
        assertThat(aggregateTask.getGroup()).isEqualTo("verification");
    }
}
