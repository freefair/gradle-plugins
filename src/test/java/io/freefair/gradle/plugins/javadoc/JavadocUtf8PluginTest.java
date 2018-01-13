package io.freefair.gradle.plugins.javadoc;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JavadocUtf8PluginTest {

    private Project project;

    @Before
    public void init() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void apply() {
        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(JavadocUtf8Plugin.class);

        Javadoc javadoc = (Javadoc) project.getTasks().getByName(JavaPlugin.JAVADOC_TASK_NAME);
        StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) javadoc.getOptions();

        assertThat(options.getDocEncoding()).isEqualToIgnoringCase("UTF-8");
        assertThat(options.getEncoding()).isEqualToIgnoringCase("UTF-8");
        assertThat(options.getCharSet()).isEqualToIgnoringCase("UTF-8");
    }
}