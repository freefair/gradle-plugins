package io.freefair.gradle.plugins.maven.javadoc;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

public class JavadocJarPluginTest {

    private Project project;

    @Before
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void apply() {
        project.getPlugins().apply(JavadocJarPlugin.class);
    }

    @Test
    public void apply_java() {
        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(JavadocJarPlugin.class);
    }
}