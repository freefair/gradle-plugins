package io.freefair.gradle.plugins.javadoc;

import io.freefair.gradle.plugins.AbstractPluginTest;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.junit.Assert.assertThat;


public class JavadocLinksPluginTest extends AbstractPluginTest {

    @Test
    public void apply() throws Exception {
        loadBuildFileFromClasspath("javadoc-links.gradle");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("javadoc", "--info")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        assertThat(result.getOutput(), CoreMatchers.containsString("http://square.github.io/okio/1.x/okio/"));
    }

}