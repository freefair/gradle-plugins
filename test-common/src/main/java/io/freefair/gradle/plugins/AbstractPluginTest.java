package io.freefair.gradle.plugins;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.freefair.gradle.plugins.builder.gradle.GradleConfigurationBuilder;
import io.freefair.gradle.plugins.builder.io.FileBuilder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class AbstractPluginTest {

    @TempDir
    protected File testProjectDir;
    protected File buildFile;

    @BeforeEach
    public void setup() throws IOException {
        buildFile = new File(testProjectDir, "build.gradle");
    }

    protected void loadBuildFileFromClasspath(String name) throws IOException {
        InputStream resourceAsStream = getClass().getResourceAsStream(name);

        Files.copy(resourceAsStream, buildFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    protected File getTemporaryDirectory() {
        return testProjectDir;
    }

    protected FileBuilder createFile(String fileName) {
        try {
            return new FileBuilder(new File(testProjectDir, fileName));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected FileBuilder createFile(String directory, String fileName) {
        try {
            File file = getFile(directory, fileName);
            return new FileBuilder(file);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private File getFile(String directory, String fileName) throws IOException {
        File root = new File(testProjectDir, directory);
        if (!root.exists() && !root.mkdirs())
            throw new RuntimeException("Error while creating directories");

        File file = new File(root, fileName);
        if (!file.exists() && !file.createNewFile())
            throw new RuntimeException("Error while creating file");
        return file;
    }

    protected void createJavaClass(String sourceSet, String packageName, TypeSpec typeSpec) {
        try {

            String replace = "src/" + sourceSet + "/java/" + packageName.replace(".", "/");
            FileBuilder file = createFile(replace, typeSpec.name + ".java");
            file.append(JavaFile.builder(packageName, typeSpec).build().toString());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected String readJavaClassFromDirectory(String direcotry, String packageName, String className) {
        try {
            if(!direcotry.endsWith("/"))
                direcotry += "/";
            String path = direcotry + packageName.replace(".", "/");
            return new String(Files.readAllBytes(getFile(path, className + ".java").toPath()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected String readJavaClass(String sourceSet, String packageName, String className) {
        try {
            String replace = "src/" + sourceSet + "/java/" + packageName.replace(".", "/");
            File file = getFile(replace, className + ".java");
            return new String(Files.readAllBytes(file.toPath()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected GradleConfigurationBuilder createGradleConfiguration() {
        return new GradleConfigurationBuilder(new FileBuilder(buildFile));
    }

    protected BuildResult executeTask(String... taskNames) {
        String[] parameters = Arrays.copyOf(taskNames, taskNames.length + 1);
        parameters[taskNames.length] = "--stacktrace";
        return GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withPluginClasspath()
                .withDebug(true)
                .withArguments(parameters).build();
    }
}
