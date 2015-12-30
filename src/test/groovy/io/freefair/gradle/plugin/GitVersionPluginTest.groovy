package io.freefair.gradle.plugin;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

import static org.junit.Assert.*;

public class GitVersionPluginTest {

    @Test
    public void testApply() throws Exception {
        Project project = ProjectBuilder.builder().build();

        project.getPluginManager().apply("io.freefair.git-version");
    }
}