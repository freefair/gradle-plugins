package io.freefair.gradle.plugins.compress;

import io.freefair.gradle.plugins.compress.internal.CompressFileOperationsImpl;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;

public class CompressFileOperationsImplTest {

    private CompressFileOperationsImpl compressFileOperations;

    @Before
    public void setUp() {
        Project project = ProjectBuilder.builder().build();
        compressFileOperations = new CompressFileOperationsImpl((ProjectInternal) project);
    }
}