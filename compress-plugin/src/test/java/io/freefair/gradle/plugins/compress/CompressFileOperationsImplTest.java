package io.freefair.gradle.plugins.compress;

import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CompressFileOperationsImplTest {

    private CompressFileOperationsImpl compressFileOperations;

    @Before
    public void setUp() {
        Project project = ProjectBuilder.builder().build();
        compressFileOperations = new CompressFileOperationsImpl((ProjectInternal) project);
    }
}