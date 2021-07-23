package io.freefair.gradle.plugins.aspectj.internal;

import io.freefair.gradle.plugins.aspectj.AspectjSourceDirectorySet;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.DefaultSourceDirectorySet;


/**
 * @author Lars Grefer
 * @see org.gradle.api.internal.tasks.DefaultGroovySourceDirectorySet
 * @see org.gradle.api.internal.tasks.DefaultScalaSourceDirectorySet
 */
public class DefaultAspectjSourceDirectorySet extends DefaultSourceDirectorySet implements AspectjSourceDirectorySet {
    public DefaultAspectjSourceDirectorySet(SourceDirectorySet sourceSet) {
        super(sourceSet);
    }
}
