package io.freefair.gradle.plugins;

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
    public void testHelloWorldTask() throws IOException {
        loadBuildFileFromClasspath("war-overlay.gradle");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("eW", "--stacktrace")
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