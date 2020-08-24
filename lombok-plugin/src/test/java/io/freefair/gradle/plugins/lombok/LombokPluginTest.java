package io.freefair.gradle.plugins.lombok;


import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LombokPluginTest {

    private Project project;

    @BeforeEach
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void apply_alone() {
        project.getPlugins().apply(LombokPlugin.class);
    }

    @Test
    public void apply_after_java() {
        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(LombokPlugin.class);
    }

    @Test
    public void apply_before_java() {
        project.getPlugins().apply(LombokPlugin.class);
        project.getPlugins().apply(JavaPlugin.class);
    }
}
