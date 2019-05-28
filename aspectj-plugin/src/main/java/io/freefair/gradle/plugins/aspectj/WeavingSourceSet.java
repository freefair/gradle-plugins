package io.freefair.gradle.plugins.aspectj;

import org.gradle.api.file.FileCollection;

public interface WeavingSourceSet {

    String getAspectConfigurationName();

    FileCollection getAspectPath();

    void setAspectPath(FileCollection aspectPath);
}
