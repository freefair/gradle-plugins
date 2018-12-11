package io.freefair.gradle.plugin.codegenerator.test;

import io.freefair.gradle.plugin.codegenerator.CodeGeneratorPlugin;
import io.freefair.gradle.plugins.AbstractPluginTest;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Objects;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class SimpleCodeGenerationTest extends AbstractPluginTest {

    private Project project;

    @Before
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test(expected = Exception.class)
    public void applyAlone() {
        project.getPlugins().apply(CodeGeneratorPlugin.class);
    }

    @Test
    public void applyAfterJava() {
        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(CodeGeneratorPlugin.class);

        TaskContainer tasks = project.getTasks();
        assertThat(tasks.parallelStream().anyMatch(t -> t.getName().equals("generateCode")), is(equalTo(true)));
    }

    @Test
    public void testBuild() {
        try {
            File resource = new File(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("test-code-generator.jar")).toURI());
            FileUtils.copyFile(resource, new File(getTemporaryDirectory(), "test-code-generator.jar"));

            createGradleConfiguration()
                    .applyPlugin("java")
                    .applyPlugin("io.freefair.code-generator")
                    .addCustomConfigurationBlock("codeGenerator {\n" +
                            "   generatorJar 'test-code-generator.jar'\n" +
                            "}")
                    .write();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        BuildResult generateCode = executeTask("generateCode");

        System.out.println(generateCode.getOutput());
        assertThat(Objects.requireNonNull(generateCode.task(":generateCode")).getOutcome(), is(equalTo(TaskOutcome.SUCCESS)));
        String javaClass = readJavaClassFromDirectory("build/generated-src/generator/main/", "io.freefair.gradle.codegen.test", "TestClass");
        System.out.println(javaClass);
        assertThat(javaClass, not(isEmptyString()));
    }
}
