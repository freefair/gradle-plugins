package io.freefair.gradle.plugins;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Objects;

public class AbstractPluginTest {

	@Rule
	public final TemporaryFolder testProjectDir = new TemporaryFolder();
	protected File buildFile;

	@Before
	public void setup() throws IOException {
		buildFile = testProjectDir.newFile("build.gradle");
	}

	protected void loadBuildFileFromClasspath(String name) throws IOException {
		InputStream resourceAsStream = getClass().getResourceAsStream(name);
		Files.copy(resourceAsStream, buildFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	protected FileBuilder createFile(String fileName) {
		try {
			return new FileBuilder(testProjectDir.newFile(fileName));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	protected FileBuilder createFile(String directory, String fileName) {
		try {
			File root = new File(testProjectDir.getRoot(), directory);
			if(!root.exists() && !root.mkdirs())
					throw new RuntimeException("Error while creating directories");

			File file = new File(root, fileName);
			if(!file.exists() && !file.createNewFile())
					throw new RuntimeException("Error while creating file");

			return new FileBuilder(file);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	protected GradleConfigurationBuilder createGradleConfiguration() {
		return new GradleConfigurationBuilder(new FileBuilder(buildFile));
	}

	protected BuildResult executeTask(String taskName) {
		return GradleRunner.create()
				.withProjectDir(testProjectDir.getRoot())
				.withPluginClasspath()
				.withDebug(true)
				.withArguments(taskName, "--stacktrace").build();
	}
}
