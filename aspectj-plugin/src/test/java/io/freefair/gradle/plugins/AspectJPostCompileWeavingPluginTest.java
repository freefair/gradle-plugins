package io.freefair.gradle.plugins;

import io.freefair.gradle.plugins.aspectj.AspectJPostCompileWeavingPlugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.GroovyPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AspectJPostCompileWeavingPluginTest {

    private Project project;

    @BeforeEach
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void apply() {
        project.getPlugins().apply(AspectJPostCompileWeavingPlugin.class);
    }

    @Test
    public void applyJava_before() {
        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(AspectJPostCompileWeavingPlugin.class);
    }

    @Test
    public void applyJava_after() {
        project.getPlugins().apply(AspectJPostCompileWeavingPlugin.class);
        project.getPlugins().apply(JavaPlugin.class);
    }

    @Test
    public void applyGroovy_before() {
        project.getPlugins().apply(GroovyPlugin.class);
        project.getPlugins().apply(AspectJPostCompileWeavingPlugin.class);
    }

    @Test
    public void applyGroovy_after() {
        project.getPlugins().apply(AspectJPostCompileWeavingPlugin.class);
        project.getPlugins().apply(GroovyPlugin.class);
    }
}
