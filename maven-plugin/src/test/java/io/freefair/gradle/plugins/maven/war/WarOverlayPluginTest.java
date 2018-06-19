package io.freefair.gradle.plugins.maven.war;

import io.freefair.gradle.plugins.AbstractPluginTest;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;

/**
 * @author Lars Grefer
 */
public class WarOverlayPluginTest extends AbstractPluginTest {

    @Test
    public void testProperties() {
        Project project = ProjectBuilder.builder().build();

        project.getPlugins().apply(WarPlugin.class);
        project.getPlugins().apply(WarOverlayPlugin.class);

        Task warTask = project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);

        assertThat(warTask.hasProperty("overlays")).isTrue();
        assertThat(warTask.property("overlays")).isInstanceOf(DomainObjectCollection.class);
    }

    @Test
    public void testHelloWorldTask() throws IOException {
        loadBuildFileFromClasspath("war-overlay.gradle");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("war", "--stacktrace")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        assertEquals(result.task(":war").getOutcome(), SUCCESS);

    }

    @Test
    public void testAttatchClassesMaven() throws IOException {
        loadBuildFileFromClasspath("war-overlay-ac-m.gradle");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("assemble")
                .withPluginClasspath()
                .build();

        assertThat(result.task(":assemble").getOutcome()).isEqualTo(SUCCESS);

        assertThat(result.task(":jar").getOutcome()).isEqualTo(SUCCESS);
        assertThat(result.task(":war").getOutcome()).isEqualTo(SUCCESS);
    }

}