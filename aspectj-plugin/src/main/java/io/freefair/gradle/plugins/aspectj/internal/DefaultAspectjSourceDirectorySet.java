package io.freefair.gradle.plugins.aspectj.internal;

import io.freefair.gradle.plugins.aspectj.AspectjSourceDirectorySet;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.DefaultSourceDirectorySet;
import org.gradle.api.internal.tasks.TaskDependencyFactory;

import javax.inject.Inject;


/**
 * @author Lars Grefer
 * @see org.gradle.api.internal.tasks.DefaultGroovySourceDirectorySet
 * @see org.gradle.api.internal.tasks.DefaultScalaSourceDirectorySet
 */
public abstract class DefaultAspectjSourceDirectorySet extends DefaultSourceDirectorySet implements AspectjSourceDirectorySet {
    @Inject
    public DefaultAspectjSourceDirectorySet(SourceDirectorySet sourceDirectorySet, TaskDependencyFactory taskDependencyFactory) {
        super(sourceDirectorySet, taskDependencyFactory);
    }
}
