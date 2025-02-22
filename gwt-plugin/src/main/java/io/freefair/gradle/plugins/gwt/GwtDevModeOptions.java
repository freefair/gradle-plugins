package io.freefair.gradle.plugins.gwt;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;

/**
 * @author Lars Grefer
 */
public interface GwtDevModeOptions extends CommonGwtToolOptions {

    /**
     * Starts a servlet container serving the directory specified by the -war flag. (defaults to ON)
     */
    @Optional
    @Input
    Property<Boolean> getStartServer();

    /**
     * Specifies the TCP port for the embedded web server (defaults to 8888)
     */
    @Optional
    @Input
    Property<Integer> getPort();

    /**
     * Logs to a file in the given directory, as well as graphically
     */
    @Optional
    @OutputDirectory
    DirectoryProperty getLogdir();

    /**
     * Debugging: causes normally-transient generated types to be saved in the specified directory
     */
    @Optional
    @OutputDirectory
    DirectoryProperty getGen();

    /**
     * Specifies the bind address for the code server and web server (defaults to 127.0.0.1)
     */
    @Optional
    @Input
    Property<String> getBindAddress();

    /**
     * Specifies the TCP port for the code server (defaults to 9997 for classic Dev Mode or 9876 for Super Dev Mode)
     */
    @Optional
    @Input
    Property<Integer> getCodeServerPort();

    /**
     * Runs Super Dev Mode instead of classic Development Mode. (defaults to ON)
     */
    @Optional
    @Input
    Property<Boolean> getSuperDevMode();

    /**
     * Specify a different embedded web server to run (must implement ServletContainerLauncher)
     */
    @Optional
    @Input
    Property<String> getServer();

    /**
     * Automatically launches the specified URL
     */
    @Optional
    @Input
    Property<String> getStartupUrl();

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
     * The subdirectory inside the war dir where DevMode will create module directories. (defaults empty for top level)
     */
    @Optional
    @Input
    Property<String> getModulePathPrefix();

}
