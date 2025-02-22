package io.freefair.gradle.plugins.gwt;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Console;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;

/**
 * @author Lars Grefer
 */
public interface CommonGwtToolOptions {

    @Console
    Property<String> getLogLevel();

    @Optional
    @Input
    Property<Boolean> getFailOnError();

    @Optional
    @OutputDirectory
    DirectoryProperty getWorkDir();

    @Optional
    @Input
    Property<String> getStyle();

    /**
     * Set the values of a property in the form of propertyName=value1[,value2...].
     */
    @Optional
    @Input
    MapProperty<String, String> getSetProperty();

    @Optional
    @Input
    Property<Boolean> getIncremental();

    @Optional
    @Input
    Property<String> getSourceLevel();

    /**
     * Generate exports for JsInterop purposes. If no -includeJsInteropExport/-excludeJsInteropExport provided, generates all exports. (defaults to OFF)
     */
    @Optional
    @Input
    Property<Boolean> getGenerateJsInteropExports();

    /**
     * Include members and classes while generating JsInterop exports. Flag could be set multiple times to expand the pattern. (The flag has only effect if exporting is enabled via -generateJsInteropExports)
     */
    @Optional
    @Input
    ListProperty<String> getIncludeJsInteropExports();

    /**
     * Include/exclude members and classes while generating JsInterop exports. Flag could be set multiple times to expand the pattern. (The flag has only effect if exporting is enabled via -generateJsInteropExports)
     */
    @Optional
    @Input
    ListProperty<String> getExcludeJsInteropExports();

    /**
     * @return EXPERIMENTAL: Specifies method display name mode for chrome devtools: NONE, ONLY_METHOD_NAME, ABBREVIATED or FULL (defaults to NONE)
     */
    @Optional
    @Input
    Property<String> getXmethodNameDisplayMode();

    /**
     * The GWT modules that the code server should compile. (Example: com.example.MyApp)
     */
    @Input
    ListProperty<String> getModule();
}
