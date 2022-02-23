package io.freefair.gradle.plugins;

import io.freefair.gradle.plugins.jsass.JSassBasePlugin;
import io.freefair.gradle.plugins.jsass.SassCompile;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Lars Grefer
 */
@Deprecated
public class JSassBasePluginTest {

    private Project project;

    @Before
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void testPrecisionConvention() {
        JSassBasePlugin basePlugin = project.getPlugins().apply(JSassBasePlugin.class);
        SassCompile testSass = project.getTasks().create("testSass", SassCompile.class);

        assertThat(testSass.getPrecision().get())
                .isEqualTo(basePlugin.getExtension().getPrecision().get());
    }

    @Test
    public void testPrecisionConvention_custom() {
        JSassBasePlugin basePlugin = project.getPlugins().apply(JSassBasePlugin.class);
        SassCompile testSass = project.getTasks().create("testSass", SassCompile.class);

        basePlugin.getExtension().getPrecision().set(42);
        assertThat(testSass.getPrecision().get()).isEqualTo(42);

        basePlugin.getExtension().getPrecision().set(55);
        assertThat(testSass.getPrecision().get()).isEqualTo(55);
    }

}
