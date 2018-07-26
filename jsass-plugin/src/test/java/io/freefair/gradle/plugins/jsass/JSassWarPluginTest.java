package io.freefair.gradle.plugins.jsass;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

public class JSassWarPluginTest {

    private Project project;

    @Before
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void apply() {
        project.getPlugins().apply(JSassWarPlugin.class);
    }
}