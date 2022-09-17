package io.freefair.gradle.plugins.mkdocs.tasks;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecSpec;

/**
 * Build the MkDocs documentation.
 */
@CacheableTask
public abstract class MkDocsBuild extends MkDocs {

    /**
     * Provide a specific MkDocs config.
     */
    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getConfigFile();

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
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getThemeDir();

    /**
     * The directory to output the result of the documentation build.
     */
    @OutputDirectory
    public abstract DirectoryProperty getSiteDir();

    public MkDocsBuild() {
        super("build");
        setDescription("Build the MkDocs documentation");
    }

    @Override
    void setArgs(ExecSpec mkdocs) {

        if (getConfigFile().isPresent()) {
            mkdocs.args("--config-file", getConfigFile().getAsFile().get().getAbsolutePath());
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

        if (getSiteDir().isPresent()) {
            mkdocs.args("--site-dir", getSiteDir().getAsFile().get().getAbsolutePath());
        }
    }
}
