package io.freefair.gradle.plugins.aspectj;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.SourceSet;

public interface WeavingSourceSet {

    String ASPECT_PATH_EXTENSION_NAME = "aspectPath";
    String IN_PATH_EXTENSION_NAME = "inPath";

    static String getAspectConfigurationName(SourceSet sourceSet) {
        return sourceSet.getTaskName("", "aspect");
    }

    static String getInpathConfigurationName(SourceSet sourceSet) {
        return sourceSet.getTaskName("", "inpath");
    }

    static ConfigurableFileCollection getAspectPath(SourceSet sourceSet) {
        return (ConfigurableFileCollection) sourceSet.getExtensions().getByName(ASPECT_PATH_EXTENSION_NAME);
    }

    static ConfigurableFileCollection getInPath(SourceSet sourceSet) {
        return (ConfigurableFileCollection) sourceSet.getExtensions().getByName(IN_PATH_EXTENSION_NAME);
    }
}
