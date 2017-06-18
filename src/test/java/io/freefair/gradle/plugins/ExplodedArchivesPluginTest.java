package io.freefair.gradle.plugins;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;

/**
 * @author Lars Grefer
 */
public class ExplodedArchivesPluginTest extends AbstractPluginTest {

    @Test
    public void testExplodeWar() throws IOException {
        loadBuildFileFromClasspath("exploded-archives.gradle");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("eW", "--stacktrace")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        assertEquals(result.task(":explodeWar").getOutcome(), SUCCESS);
    }

    @Test
    public void testExplodeWar_withoutExtension() throws IOException {
        loadBuildFileFromClasspath("exploded-archives_withoutExt.gradle");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("eW", "--stacktrace")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        assertEquals(result.task(":explodeWar").getOutcome(), SUCCESS);

        String name = testProjectDir.getRoot().getName();

        File targetDir = new File(testProjectDir.getRoot(), "build/libs/exploded/" + name);

        assertThat(targetDir).exists();
        assertThat(targetDir).isDirectory();
    }

    @Test
    public void testExplodeWar_withExtension() throws IOException {
        loadBuildFileFromClasspath("exploded-archives_withExt.gradle");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("eW", "--stacktrace")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        assertEquals(result.task(":explodeWar").getOutcome(), SUCCESS);

        String name = testProjectDir.getRoot().getName() + ".war";

        File targetDir = new File(testProjectDir.getRoot(), "build/libs/exploded/" + name);

        assertThat(targetDir).exists();
        assertThat(targetDir).isDirectory();
    }

    @Test
    public void testExplodeWar_customDir() throws IOException {
        loadBuildFileFromClasspath("exploded-archives_customDir.gradle");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("eW", "--stacktrace")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        assertEquals(result.task(":explodeWar").getOutcome(), SUCCESS);

        File targetDir = new File(testProjectDir.getRoot(), "build/foo");

        assertThat(targetDir).exists();
        assertThat(targetDir).isDirectory();
    }

}