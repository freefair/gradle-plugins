package io.freefair.gradle.plugins.mkdocs;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MkDocsPluginTest {

    private Project project;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    void testApply() {
        project.getPlugins().apply(MkDocsPlugin.class);
    }
}
