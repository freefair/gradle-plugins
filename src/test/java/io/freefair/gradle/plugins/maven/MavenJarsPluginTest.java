package io.freefair.gradle.plugins.maven;

import io.freefair.gradle.plugins.AbstractPluginTest;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class MavenJarsPluginTest extends AbstractPluginTest {

    @Test
    public void apply() throws Exception {
        loadBuildFileFromClasspath("/maven-jars.gradle");

        BuildResult buildResult = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("assemble", "--stacktrace")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        assertEquals(TaskOutcome.SUCCESS, buildResult.task(":sourcesJar").getOutcome());
    }

}