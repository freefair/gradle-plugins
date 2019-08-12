package io.freefair.gradle.plugins.mkdocs.tasks;

import lombok.Getter;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.CommandLineArgumentProvider;

import java.util.LinkedList;

/**
 * Run the builtin development server.
 */
@Getter
public class MkDocsServe extends MkDocs {

    /**
     * Provide a specific MkDocs config.
     */
    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    private final RegularFileProperty configFile = getProject().getObjects().fileProperty();

    /**
     * IP address and port to serve documentation locally
     * (default: localhost:8000)
     */
    @Input
    @Optional
    private final Property<String> devAddr = getProject().getObjects().property(String.class).convention("localhost:8080");

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
    private final DirectoryProperty themeDir = getProject().getObjects().directoryProperty();

    /**
     * Enable the live reloading in the development server
     */
    @Optional
    @Input
    private final Property<Boolean> livereload = getProject().getObjects().property(Boolean.class);

    /**
     * Enable the live reloading in the development server,
     * but only re-build files that have changed.
     */
    @Optional
    @Input
    private final Property<Boolean> dirtyreload = getProject().getObjects().property(Boolean.class);

    @SuppressWarnings("UnstableApiUsage")
    public MkDocsServe() {
        super("serve");

        getArgumentProviders().add((CommandLineArgumentProvider) () -> {
            LinkedList<String> args = new LinkedList<>();

            if (getConfigFile().isPresent()) {
                args.add("--config-file");
                args.add(getConfigFile().getAsFile().get().getAbsolutePath());
            }

            if (getDevAddr().isPresent()) {
                args.add("--dev-addr");
                args.add(getDevAddr().get());
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

            if (getLivereload().isPresent()) {
                if (getLivereload().get()) {
                    args.add("--livereload");
                }
                else {
                    args.add("--no-livereload");
                }
            }

            if (getLivereload().getOrElse(false)) {
                args.add("--dirtyreload");
            }

            return args;
        });
    }
}
