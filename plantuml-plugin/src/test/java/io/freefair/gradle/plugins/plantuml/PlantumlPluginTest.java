package io.freefair.gradle.plugins.plantuml;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlantumlPluginTest {

    @Test
    void apply() {
        Project project = ProjectBuilder.builder().build();

        project.getPlugins().apply(PlantumlPlugin.class);

        assertThat(project.getTasks().getNames()).contains("plantUml");
    }

    @Test
    void execute() {
        Project project = ProjectBuilder.builder().build();

        project.getPlugins().apply(PlantumlPlugin.class);

        PlantumlTask task = project.getTasks().withType(PlantumlTask.class).getByName("plantUml");

        task.source(getClass().getClassLoader().getResource("puml-files").getPath());

        assertThrows(UnsupportedOperationException.class, task::execute);
    }

    @Test
    void taskExecution() {
        Project project = ProjectBuilder.builder().build();

        PlantumlAction action = new PlantumlAction() {
            @Override
            public PlantumlParameters getParameters() {
                return new PlantumlParameters() {
                    @Override
                    public RegularFileProperty getInputFile() {
                        RegularFileProperty result = project.getObjects().fileProperty();
                        result.set(new File(getClass().getClassLoader().getResource("puml-files/test.puml").getPath()));
                        return result;
                    }

                    @Override
                    public DirectoryProperty getOutputDirectory() {
                        DirectoryProperty result = project.getObjects().directoryProperty();
                        result.set(new File(getClass().getClassLoader().getResource("puml-files").getPath()));
                        return result;
                    }

                    @Override
                    public Property<String> getFileFormat() {
                        Property<String> property = project.getObjects().property(String.class);
                        property.set("PNG");
                        return property;
                    }

                    @Override
                    public Property<Boolean> getWithMetadata() {
                        Property<Boolean> property = project.getObjects().property(Boolean.class);
                        property.set(false);
                        return property;
                    }
                };
            }
        };

        action.execute();
    }
}
