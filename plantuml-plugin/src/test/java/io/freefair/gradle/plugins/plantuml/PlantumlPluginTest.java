package io.freefair.gradle.plugins.plantuml;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.internal.impldep.org.junit.Before;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlantumlPluginTest {

    private Project project;

    @BeforeEach
    void init() {
        project = ProjectBuilder.builder().build();
        project.getRepositories().mavenCentral();
    }

    @Test
    void apply() {
        project.getPlugins().apply(PlantumlPlugin.class);

        assertThat(project.getTasks().getNames()).contains("plantUml");
    }

    @Test
    void execute() {
        project.getPlugins().apply(PlantumlPlugin.class);

        PlantumlTask task = project.getTasks().withType(PlantumlTask.class).getByName("plantUml");

        task.source(getClass().getClassLoader().getResource("puml-files").getPath());

        task.getDeleteOutputBeforeBuild().set(true);

        assertThrows(UnsupportedOperationException.class, task::execute);
    }

    @Test
    void executeWithoutDelete() {
        project.getPlugins().apply(PlantumlPlugin.class);

        PlantumlTask task = project.getTasks().withType(PlantumlTask.class).getByName("plantUml");

        task.source(getClass().getClassLoader().getResource("puml-files").getPath());

        task.getDeleteOutputBeforeBuild().set(false);

        assertThrows(UnsupportedOperationException.class, task::execute);
    }

    @Test
    void taskExecution(@TempDir File tempDir) {
        PlantumlParameters parameters = project.getObjects().newInstance(PlantumlParameters.class);

        parameters.getInputFile().set(new File(getClass().getClassLoader().getResource("puml-files/test.puml").getPath()));
        parameters.getOutputDirectory().set(tempDir);
        parameters.getFileFormat().set("PNG");
        parameters.getWithMetadata().set(false);

        PlantumlAction action = new PlantumlAction() {
            @Override
            public PlantumlParameters getParameters() {
                return parameters;
            }
        };

        action.execute();
    }
}
