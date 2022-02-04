package io.freefair.gradle.plugins.maven.war;

import org.gradle.api.Project;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.bundling.War;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WarAttachClassesPluginTest {

    private Project project;

    @BeforeEach
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void testProperties() {
        project.getPlugins().apply(WarPlugin.class);
        project.getPlugins().apply(WarAttachClassesPlugin.class);

        War warTask = (War) project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);

        assertThat(warTask.hasProperty("attachClasses")).isTrue();
        assertThat(warTask.property("attachClasses")).isInstanceOf(Property.class);

        assertThat(warTask.hasProperty("classesClassifier")).isTrue();
        assertThat(warTask.property("classesClassifier")).isInstanceOf(Property.class);
    }
}
