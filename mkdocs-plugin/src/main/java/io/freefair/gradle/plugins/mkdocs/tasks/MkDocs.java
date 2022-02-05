package io.freefair.gradle.plugins.mkdocs.tasks;

import lombok.Getter;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Console;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.Optional;
import org.gradle.process.CommandLineArgumentProvider;

import java.util.LinkedList;

@SuppressWarnings("UnstableApiUsage")
@Getter
public abstract class MkDocs extends Exec {

    /**
     * Silence warnings.
     */
    @Console
    private final Property<Boolean> quiet = getProject().getObjects().property(Boolean.class);

    /**
     * Enable verbose output.
     */
    @Console
    private final Property<Boolean> verbose = getProject().getObjects().property(Boolean.class);

    public MkDocs(String command) {
        setExecutable("mkdocs");
        getArgumentProviders().add((CommandLineArgumentProvider) () -> {
            LinkedList<String> args = new LinkedList<>();

            args.add(command);

            if (getQuiet().getOrElse(false)) {
                args.add("--quiet");
            }

            if (getVerbose().getOrElse(false)) {
                args.add("--verbose");
            }

            return args;
        });
    }
}
