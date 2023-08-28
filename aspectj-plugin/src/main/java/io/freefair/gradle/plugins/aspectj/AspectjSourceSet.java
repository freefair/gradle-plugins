package io.freefair.gradle.plugins.aspectj;

import org.gradle.api.tasks.SourceSet;

/**
 * @see org.gradle.api.tasks.GroovySourceSet
 * @see org.gradle.api.tasks.ScalaSourceSet
 */
public interface AspectjSourceSet extends WeavingSourceSet {

    static AspectjSourceDirectorySet getAspectj(SourceSet sourceSet) {
        return (AspectjSourceDirectorySet) sourceSet.getExtensions().getByName("aspectj");
    }
}
