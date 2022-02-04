package io.freefair.gradle.plugins.compress.internal;

import org.gradle.api.Project;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;


public class CompressFileOperationsImplTest {

    private CompressFileOperationsImpl compressFileOperations;

    @BeforeEach
    public void setUp() {
        Project project = ProjectBuilder.builder().build();
        compressFileOperations = new CompressFileOperationsImpl((ProjectInternal) project);
    }

    @Test
    @Disabled
    public void arTree() {
        URL resource = getClass().getResource("/test.ar");

        FileTree files = compressFileOperations.arTree(resource);

        assertThat(files.getFiles()).anyMatch(file -> file.getPath().endsWith("src/test.txt"));
    }

    @Test
    public void cpioTree() {
        URL resource = getClass().getResource("/test.cpio");

        FileTree files = compressFileOperations.cpioTree(resource);

        assertThat(files.getFiles()).anyMatch(file -> file.getPath().endsWith("src/test.txt"));
    }

    @Test
    public void sevenZipTree() {
        URL resource = getClass().getResource("/test.7z");

        FileTree files = compressFileOperations.sevenZipTree(resource);

        assertThat(files.getFiles()).anyMatch(file -> file.getPath().endsWith("src/test.txt"));
    }
}
