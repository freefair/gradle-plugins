package io.freefair.gradle.plugins;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AbstractPluginTest {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();
    protected File buildFile;

    @Before
    public void setup() throws IOException {
        buildFile = testProjectDir.newFile("build.gradle");
    }

    @SuppressFBWarnings("UI_INHERITANCE_UNSAFE_GETRESOURCE")
    protected void loadBuildFileFromClasspath(String name) throws IOException {
        InputStream resourceAsStream = getClass().getResourceAsStream(name);
        Files.copy(resourceAsStream, buildFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
