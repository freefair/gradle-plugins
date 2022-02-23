package io.freefair.gradle.plugins.aspectj;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.SourceSet;

public interface WeavingSourceSet {

    String ASPECT_PATH_EXTENSION_NAME = "aspectPath";
    String IN_PATH_EXTENSION_NAME = "inPath";

    /**
     * @deprecated Use {@link #getAspectConfigurationName(SourceSet)} instead.
     */
    @Deprecated
    String getAspectConfigurationName();

    /**
     * @deprecated Use {@link #getInpathConfigurationName(SourceSet)} instead.
     */
    @Deprecated
    String getInpathConfigurationName();

    /**
     * @deprecated Use {@link #getAspectPath(SourceSet)} instead.
     */
    @Deprecated
    FileCollection getAspectPath();

    /**
     * @deprecated Use {@link #getAspectPath(SourceSet)} and {@link ConfigurableFileCollection#from(Object...)} instead.
     */
    @Deprecated
    void setAspectPath(FileCollection aspectPath);

    /**
     * @deprecated Use {@link #getInPath(SourceSet)} instead.
     */
    @Deprecated
    FileCollection getInPath();

    /**
     * @deprecated Use {@link #getInPath(SourceSet)} and {@link ConfigurableFileCollection#from(Object...)} instead.
     */
    @Deprecated
    void setInPath(FileCollection inPath);

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
