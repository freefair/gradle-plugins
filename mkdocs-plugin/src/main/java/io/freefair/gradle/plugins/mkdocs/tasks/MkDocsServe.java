package io.freefair.gradle.plugins.mkdocs.tasks;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecSpec;

/**
 * Run the builtin development server.
 */
public abstract class MkDocsServe extends MkDocs {

    /**
     * Provide a specific MkDocs config.
     */
    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getConfigFile();

    /**
     * IP address and port to serve documentation locally
     * (default: localhost:8000)
     */
    @Input
    @Optional
    public abstract Property<String> getDevAddr();

    /**
     * Enable strict mode.
     * This will cause MkDocs to abort the build on any warnings.
     */
    @Optional
    @Input
    public abstract Property<Boolean> getStrict();

    /**
     * The theme to use when building your documentation.
     */
    @Optional
    @Input
    public abstract Property<String> getTheme();

    /**
     * The theme directory to use when building your documentation.
     */
    @Optional
    @InputDirectory
    public abstract DirectoryProperty getThemeDir();

    /**
     * Enable the live reloading in the development server
     */
    @Optional
    @Input
    public abstract Property<Boolean> getLivereload();

    /**
     * Enable the live reloading in the development server,
     * but only re-build files that have changed.
     */
    @Optional
    @Input
    public abstract Property<Boolean> getDirtyreload();

    public MkDocsServe() {
        super("serve");
        setDescription("Run the builtin development server.");
        getDevAddr().convention("localhost:8080");
    }

    @Override
    void setArgs(ExecSpec mkdocs) {
        if (getConfigFile().isPresent()) {
            mkdocs.args("--config-file", getConfigFile().getAsFile().get().getAbsolutePath());
        }

        if (getDevAddr().isPresent()) {
            mkdocs.args("--dev-addr", getDevAddr().get());
        }

        if (getStrict().getOrElse(false)) {
            mkdocs.args("--strict");
        }

        if (getTheme().isPresent()) {
            mkdocs.args("--theme", getTheme().get());
        }

        if (getThemeDir().isPresent()) {
            mkdocs.args("--theme-dir", getThemeDir().getAsFile().get().getAbsolutePath());
        }

        if (getLivereload().isPresent()) {
            if (getLivereload().get()) {
                mkdocs.args("--livereload");
            }
            else {
                mkdocs.args("--no-livereload");
            }
        }

        if (getDirtyreload().getOrElse(false)) {
            mkdocs.args("--dirtyreload");
        }
    }
}
