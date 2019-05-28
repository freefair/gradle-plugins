package io.freefair.gradle.plugins.aspectj;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;

import javax.annotation.Nullable;

/**
 * @see org.gradle.api.tasks.GroovySourceSet
 * @see org.gradle.api.tasks.ScalaSourceSet
 */
public interface AspectjSourceSet extends WeavingSourceSet {

    /**
     * Returns the source to be compiled by the Groovy compiler for this source set. Any Java source present in this set
     * will be passed to the Groovy compiler for joint compilation.
     *
     * @return The Groovy/Java source. Never returns null.
     */
    SourceDirectorySet getAspectj();

    /**
     * Configures the Groovy source for this set.
     *
     * <p>The given closure is used to configure the {@link SourceDirectorySet} which contains the Groovy source.
     *
     * @param configureClosure The closure to use to configure the Groovy source.
     * @return this
     */
    AspectjSourceSet aspectj(@Nullable Closure configureClosure);

    /**
     * Configures the Groovy source for this set.
     *
     * <p>The given action is used to configure the {@link SourceDirectorySet} which contains the Groovy source.
     *
     * @param configureAction The action to use to configure the Groovy source.
     * @return this
     */
    AspectjSourceSet aspectj(Action<? super SourceDirectorySet> configureAction);

    /**
     * All Groovy source for this source set.
     *
     * @return the Groovy source. Never returns null.
     */
    SourceDirectorySet getAllAspectj();
}
