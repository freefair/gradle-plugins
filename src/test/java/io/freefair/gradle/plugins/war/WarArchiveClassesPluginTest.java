package io.freefair.gradle.plugins.war;

import org.gradle.api.Project;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.bundling.War;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WarArchiveClassesPluginTest {

    private Project project;

    @Before
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void testArchiveClassesProperty() {
        project.getPlugins().apply(WarPlugin.class);
        project.getPlugins().apply(WarArchiveClassesPlugin.class);

        War warTask = (War) project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);

        assertThat(warTask.hasProperty("archiveClasses")).isTrue();
        assertThat(warTask.property("archiveClasses")).isInstanceOf(Boolean.class);
    }

}