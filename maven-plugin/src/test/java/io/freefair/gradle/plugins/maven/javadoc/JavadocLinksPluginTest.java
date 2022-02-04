package io.freefair.gradle.plugins.maven.javadoc;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JavadocLinksPluginTest {

    private Project project;

    @BeforeEach
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void apply() {
        project.getPlugins().apply(JavadocLinksPlugin.class);
    }

    @Test
    public void apply_java() {
        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(JavadocLinksPlugin.class);
    }
}
