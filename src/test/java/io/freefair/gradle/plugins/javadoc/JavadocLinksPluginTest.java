package io.freefair.gradle.plugins.javadoc;

import io.freefair.gradle.plugins.AbstractPluginTest;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


public class JavadocLinksPluginTest extends AbstractPluginTest {

    @Test
    public void testRetrofitOkio() throws Exception {
        testLink("com.squareup.retrofit2:retrofit:2+", "http://square.github.io/okio/1.x/okio/");
    }

    @Test
    public void testJavaEE6() throws IOException {
        testLink("javax:javaee-api:6.0", "https://docs.oracle.com/javaee/7/api/");
    }

    @Test
    public void testJavaEE7() throws IOException {
        testLink("javax:javaee-api:7.0", "https://docs.oracle.com/javaee/7/api/");
    }

    public void testLink(String dep, String link) throws IOException {
        loadBuildFileFromClasspath("/javadoc-links.gradle");

        List<String> lines = Arrays.asList(
                "",
                "dependencies {",
                "compile '" + dep + "'",
                "}"
        );

        Files.write(buildFile.toPath(), lines, Charset.defaultCharset(), StandardOpenOption.APPEND);


        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("javadoc", "--info")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        assertThat(result.getOutput(), CoreMatchers.containsString(link));

    }

}