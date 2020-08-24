package io.freefair.gradle.plugin.codegenerator.test;

import io.freefair.gradle.plugin.codegenerator.CodeGeneratorPlugin;
import io.freefair.gradle.plugins.AbstractPluginTest;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SimpleCodeGenerationTest extends AbstractPluginTest {

    private Project project;

    @BeforeEach
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void applyAlone() {
        assertThrows(Exception.class, () -> {
            project.getPlugins().apply(CodeGeneratorPlugin.class);
        });
    }

    @Test
    public void applyAfterJava() {
        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(CodeGeneratorPlugin.class);

        TaskContainer tasks = project.getTasks();
        assertThat(tasks).anyMatch(t -> t.getName().equals("generateCode"));
    }

    @Test
    @Disabled
    public void testBuild() {
        try {
            File resource = new File(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("test-code-generator.jar")).toURI());
            Files.copy(resource.toPath(), new File(getTemporaryDirectory(), "test-code-generator.jar").toPath());

            createGradleConfiguration()
                    .applyPlugin("java")
                    .applyPlugin("io.freefair.code-generator")
                    .addDependency("codeGenerator", "files('test-code-generator.jar')")
                    .write();

            new File(getTemporaryDirectory(), "src/code-generator/main").mkdirs();
            new File(getTemporaryDirectory(), "build").mkdirs();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        BuildResult generateCode = executeTask("generateCode");

        System.out.println(generateCode.getOutput());
        assertThat(Objects.requireNonNull(generateCode.task(":generateCode")).getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        String javaClass = readJavaClassFromDirectory("build/generated-src/generator/main/", "io.freefair.gradle.codegen.test", "TestClass");
        System.out.println(javaClass);
        assertThat(javaClass).isNotEmpty();
    }
}
