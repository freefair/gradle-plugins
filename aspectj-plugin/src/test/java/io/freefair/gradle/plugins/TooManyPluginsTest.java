package io.freefair.gradle.plugins;


import io.freefair.gradle.plugins.aspectj.AspectJPlugin;
import io.freefair.gradle.plugins.aspectj.AspectJPostCompileWeavingPlugin;
import org.gradle.api.Project;
import org.gradle.api.internal.plugins.PluginApplicationException;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TooManyPluginsTest {

    private Project project;

    @BeforeEach
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    void normal_post() {
        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(AspectJPlugin.class);
        Assertions.assertThrows(PluginApplicationException.class, () -> {
            project.getPlugins().apply(AspectJPostCompileWeavingPlugin.class);
        });
    }

    @Test
    void post_normal() {
        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(AspectJPostCompileWeavingPlugin.class);
        Assertions.assertThrows(PluginApplicationException.class, () -> {
            project.getPlugins().apply(AspectJPlugin.class);
        });
    }

}
