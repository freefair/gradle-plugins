package io.freefair.gradle.plugins.compress;

import io.freefair.gradle.plugins.compress.internal.CompressFileOperationsImpl;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Lars Grefer
 */
public class CompressTreePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getConvention().create(CompressFileOperations.class, "compressTree", CompressFileOperationsImpl.class, project);
    }
}
