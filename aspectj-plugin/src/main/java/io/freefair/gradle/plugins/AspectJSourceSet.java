package io.freefair.gradle.plugins;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.tasks.DefaultGroovySourceSet;
import org.gradle.api.tasks.GroovySourceSet;

/**
 * @author Lars Grefer
 * @see DefaultGroovySourceSet
 * @see GroovySourceSet
 */
@Getter
@Setter
public class AspectJSourceSet {

    private String aspectConfigurationName;

    private FileCollection aspectPath;
}
