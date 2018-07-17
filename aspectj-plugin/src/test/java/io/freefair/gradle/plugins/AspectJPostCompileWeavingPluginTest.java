package io.freefair.gradle.plugins;

import org.gradle.api.Project;
import org.gradle.api.plugins.GroovyPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

public class AspectJPostCompileWeavingPluginTest {

    private Project project;

    @Before
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void apply() {
        project.getPlugins().apply(AspectJPostCompileWeavingPlugin.class);
    }

    @Test
    public void apply_java() {
        project.getPlugins().apply(AspectJPostCompileWeavingPlugin.class);
        project.getPlugins().apply(JavaPlugin.class);
    }

    @Test
    public void apply_groovy() {
        project.getPlugins().apply(AspectJPostCompileWeavingPlugin.class);
        project.getPlugins().apply(GroovyPlugin.class);
    }
}