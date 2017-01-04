package io.freefair.gradle.plugins;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.*;

/**
 * @author Lars Grefer
 */
public class WarOverlayPluginTest extends AbstractPluginTest {

    @Test
    public void testHelloWorldTask() throws IOException {
        loadBuildFileFromClasspath("/test1.gradle");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("eW", "--stacktrace")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        assertEquals(result.task(":war").getOutcome(), SUCCESS);

    }

}