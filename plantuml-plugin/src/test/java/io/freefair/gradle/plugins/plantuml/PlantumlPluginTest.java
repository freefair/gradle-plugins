package io.freefair.gradle.plugins.plantuml;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PlantumlPluginTest {

    @Test
    void apply() {
        Project project = ProjectBuilder.builder().build();

        project.getPlugins().apply(PlantumlPlugin.class);

        assertThat(project.getTasks().getNames()).contains("plantUml");
    }
}
