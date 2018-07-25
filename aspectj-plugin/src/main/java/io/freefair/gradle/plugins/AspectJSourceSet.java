package io.freefair.gradle.plugins;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.file.FileCollection;

@Getter
@Setter
public class AspectJSourceSet {

    private String aspectConfigurationName;

    private FileCollection aspectPath;
}
