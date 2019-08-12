package io.freefair.gradle.plugins.mkdocs.tasks;

import lombok.Getter;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.CommandLineArgumentProvider;

import java.util.LinkedList;

/**
 * Build the MkDocs documentation.
 */
@SuppressWarnings("UnstableApiUsage")
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
    @PathSensitive(PathSensitivity.RELATIVE)
    private final DirectoryProperty siteDir = getProject().getObjects().directoryProperty();

    public MkDocsBuild() {
        super("build");
        getArgumentProviders().add((CommandLineArgumentProvider) () -> {
            LinkedList<String> args = new LinkedList<>();

            if (getConfigFile().isPresent()) {
                args.add("--config-file");
                args.add(getConfigFile().getAsFile().get().getAbsolutePath());
            }

            if (getStrict().getOrElse(false)) {
                args.add("--strict");
            }

            if (getTheme().isPresent()) {
                args.add("--theme");
                args.add(getTheme().get());
            }

            if (getThemeDir().isPresent()) {
                args.add("--theme-dir");
                args.add(getThemeDir().getAsFile().get().getAbsolutePath());
            }

            if (getSiteDir().isPresent()) {
                args.add("--site-dir");
                args.add(getSiteDir().getAsFile().get().getAbsolutePath());
            }

            return args;
        });
    }
}
