package io.freefair.gradle.plugins.lombok;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LombokBasePluginTest {

    @Test
    public void lombok_configuration() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply(LombokBasePlugin.class);

        Configuration lombok = project.getConfigurations().findByName("lombok");
        assertThat(lombok).as("lombok configuration is created").isNotNull();
        assertThat(lombok.isCanBeDeclared()).as("can have dependencies declared").isTrue();
        assertThat(lombok.isCanBeResolved()).as("can resolve dependencies").isTrue();
        assertThat(lombok.isCanBeConsumed()).as("is not exposed to be consumed by other projects").isFalse();
    }
}
