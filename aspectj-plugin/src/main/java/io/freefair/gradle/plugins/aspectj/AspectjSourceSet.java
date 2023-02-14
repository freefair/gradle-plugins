package io.freefair.gradle.plugins.aspectj;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.SourceSet;

import javax.annotation.Nullable;

/**
 * @see org.gradle.api.tasks.GroovySourceSet
 * @see org.gradle.api.tasks.ScalaSourceSet
 */
public interface AspectjSourceSet extends WeavingSourceSet {

    static AspectjSourceDirectorySet getAspectj(SourceSet sourceSet) {
        return (AspectjSourceDirectorySet) sourceSet.getExtensions().getByName("aspectj");
    }
}
