package io.freefair.gradle.plugins.gwt;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;

/**
 * @author Lars Grefer
 */
public interface GwtCompileOptions extends CommonGwtToolOptions {

    /**
     * EXPERIMENTAL: Enables Javascript output suitable for post-compilation by Closure Compiler (defaults to OFF)
     */
    @Optional
    @Input
    Property<Boolean> getXClosureFormattedOutput();

    /**
     * Compile a report that tells the "Story of Your Compile". (defaults to OFF)
     */
    @Optional
    @Input
    Property<Boolean> getCompileReport();

    /**
     * EXPERIMENTAL: DEPRECATED: use jre.checks.checkLevel instead. (defaults to OFF)
     */
    @Optional
    @Input
    Property<Boolean> getXcheckCasts();

    /**
     * EXPERIMENTAL: Include metadata for some java.lang.Class methods (e.g. getName()). (defaults to ON)
     */
    @Optional
    @Input
    Property<Boolean> getXclassMetadata();

    /**
     * Compile quickly with minimal optimizations. (defaults to OFF)
     */
    @Optional
    @Input
    Property<Boolean> getDraftCompile();

    /**
     * Include assert statements in compiled output. (defaults to OFF)
     */
    @Optional
    @Input
    Property<Boolean> getCheckAssertions();

    /**
     * EXPERIMENTAL: Limits of number of fragments using a code splitter that merges split points.
     */
    @Optional
    @Input
    Property<Integer> getXfragmentCount();

    /**
     * Debugging: causes normally-transient generated types to be saved in the specified directory
     */
    @Optional
    @OutputDirectory
    DirectoryProperty getGen();

    /**
     * Puts most JavaScript globals into namespaces. Default: PACKAGE for -draftCompile, otherwise NONE
     */
    @Optional
    @Input
    Property<String> getXnamespace();

    /**
     * Sets the optimization level used by the compiler.  0=none 9=maximum.
     */
    @Optional
    @Input
    Property<Integer> getOptimize();

    /**
     * Enables saving source code needed by debuggers. Also see -debugDir. (defaults to OFF)
     */
    @Optional
    @Input
    Property<Boolean> getSaveSource();

    /**
     * Validate all source code, but do not compile. (defaults to OFF)
     */
    @Optional
    @Input
    Property<Boolean> getValidateOnly();

    /**
     * The number of local workers to use when compiling permutations
     */
    @Optional
    @Input
    Property<Integer> getLocalWorkers();

    /**
     * The directory into which deployable output files will be written (defaults to 'war')
     */
    @OutputDirectory
    DirectoryProperty getWar();

    /**
     * The directory into which deployable but not servable output files will be written (defaults to 'WEB-INF/deploy' under the -war directory/jar, and may be the same as the -extra directory/jar)
     */
    @Optional
    @OutputDirectory
    DirectoryProperty getDeploy();

    /**
     * The directory into which extra files, not intended for deployment, will be written
     */
    @Optional
    @OutputDirectory
    DirectoryProperty getExtra();

    /**
     * Overrides where source files useful to debuggers will be written. Default: saved with extras.
     */
    @Optional
    @OutputDirectory
    DirectoryProperty getSaveSourceOutput();


}
