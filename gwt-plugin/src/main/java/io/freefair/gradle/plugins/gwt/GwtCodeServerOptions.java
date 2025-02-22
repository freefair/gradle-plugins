package io.freefair.gradle.plugins.gwt;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

/**
 * @author Lars Grefer
 */
public interface GwtCodeServerOptions extends CommonGwtToolOptions {

    /**
     * Allows -src flags to reference missing directories. (defaults to OFF)
     */
    @Optional
    @Input
    Property<Boolean> getAllowMissingSrc();

    /**
     * Exits after compiling the modules. The exit code will be 0 if the compile succeeded. (defaults to OFF)
     */
    @Optional
    @Input
    Property<Boolean> getCompileTest();

    /**
     * The number of times to recompile (after the first one) during a compile test.
     */
    @Optional
    @Input
    Property<Integer> getCompileTestRecompiles();

    /**
     * Precompile modules. (defaults to ON)
     */
    @Optional
    @Input
    Property<Boolean> getPrecompile();

    /**
     * The port where the code server will run.
     */
    @Optional
    @Input
    Property<Integer> getPort();

    /**
     * A directory containing GWT source to be prepended to the classpath for compiling.
     */
    @Optional
    @InputDirectory
    DirectoryProperty getSrc();

    /**
     * An output directory where files for launching Super Dev Mode will be written. (Optional.)
     */
    @Optional
    @OutputDirectory
    DirectoryProperty getLauncherDir();

    /**
     * Specifies the bind address for the code server and web server (defaults to 127.0.0.1)
     */
    @Optional
    @Input
    Property<String> getBindAddress();

    /**
     * EXPERIMENTAL: Enables Javascript output suitable for post-compilation by Closure Compiler (defaults to OFF)
     */
    @Optional
    @Input
    Property<Boolean> getXclosureFormattedOutput();



}
