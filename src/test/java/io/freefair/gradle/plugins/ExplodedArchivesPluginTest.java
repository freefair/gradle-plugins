package io.freefair.gradle.plugins;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.*;

/**
 * Created by larsgrefer on 21.12.16.
 */
public class ExplodedArchivesPluginTest {
    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();
    private File buildFile;

    @Before
    public void setup() throws IOException {
        buildFile = testProjectDir.newFile("build.gradle");
    }

    @Test
    public void testHelloWorldTask() throws IOException {
        InputStream resourceAsStream = getClass().getResourceAsStream("/exploded-archives.gradle");
        Files.copy(resourceAsStream, buildFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("eW", "--stacktrace")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        assertEquals(result.task(":explodeWar").getOutcome(), SUCCESS);

    }

}