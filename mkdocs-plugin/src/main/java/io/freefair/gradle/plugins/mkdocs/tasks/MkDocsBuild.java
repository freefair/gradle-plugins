package io.freefair.gradle.plugins.mkdocs.tasks;

import lombok.Getter;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecSpec;

/**
 * Build the MkDocs documentation.
 */
@Getter
@CacheableTask
public class MkDocsBuild extends MkDocs {

    /**
     * Provide a specific MkDocs config.
     */
    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    private final RegularFileProperty configFile = getProject().getObjects().fileProperty();

    /**
     * Enable strict mode.
     * This will cause MkDocs to abort the build on any warnings.
     */
    @Optional
    @Input
    private final Property<Boolean> strict = getProject().getObjects().property(Boolean.class);

    /**
     * The theme to use when building your documentation.
     */
    @Optional
    @Input
    private final Property<String> theme = getProject().getObjects().property(String.class);

    /**
     * The theme directory to use when building your documentation.
     */
    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    private final DirectoryProperty themeDir = getProject().getObjects().directoryProperty();

    /**
     * The directory to output the result of the documentation build.
     */
    @OutputDirectory
    private final DirectoryProperty siteDir = getProject().getObjects().directoryProperty();

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
