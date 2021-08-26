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
        //noinspection deprecation
        project.getConvention().create(CompressFileOperations.class, "compressTree", CompressFileOperationsImpl.class, project);

        project.getExtensions().create(CompressFileOperations.class, "commonsCompress", CompressFileOperationsImpl.class, project);
    }
}
