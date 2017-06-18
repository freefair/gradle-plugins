package io.freefair.gradle.plugins.maven;

import io.freefair.gradle.plugins.AbstractPluginTest;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;

/**
 * @author Lars Grefer
 */
public class SourcesJarPluginTest extends AbstractPluginTest {

    @Test
    public void apply() throws Exception {
        loadBuildFileFromClasspath("sources-jar.gradle");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("assemble", "--stacktrace")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        assertEquals(result.task(":sourcesJar").getOutcome(), SUCCESS);

    }

}