package io.freefair.gradle.plugins.aspectj;

import org.gradle.api.file.FileCollection;

public interface WeavingSourceSet {

    String getAspectConfigurationName();
    String getInpathConfigurationName();

    FileCollection getAspectPath();
    void setAspectPath(FileCollection aspectPath);

    FileCollection getInPath();
    void setInPath(FileCollection inPath);
}
