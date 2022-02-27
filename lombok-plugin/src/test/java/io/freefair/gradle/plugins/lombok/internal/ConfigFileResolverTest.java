package io.freefair.gradle.plugins.lombok.internal;

import org.gradle.api.file.FileCollection;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ConfigFileResolverTest {

    private ConfigFileResolver configFileResolver;

    @BeforeEach
    void setUp() {
        configFileResolver = new ConfigFileResolver(ProjectBuilder.builder().build().getObjects());
    }

    @Test
    void findConfigFiles() {
        File dir = new File(".");
        FileCollection configFiles = configFileResolver.findConfigFiles(dir);

        System.out.printf("Found %s for %s%n", configFiles.getFiles(), dir.getAbsolutePath());

        assertThat(configFiles).isNotEmpty();
    }
}
