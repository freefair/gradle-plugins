package io.freefair.gradle.plugins.mkdocs.tasks;

import lombok.Getter;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.process.CommandLineArgumentProvider;

import java.util.LinkedList;

/**
 * Create a new MkDocs project
 */
@Getter
public class MkDocsNew extends MkDocs {

    @Optional
    @OutputDirectory
    private final DirectoryProperty projectDirectory = getProject().getObjects().directoryProperty();

    @SuppressWarnings("UnstableApiUsage")
    public MkDocsNew() {
        super("new");

        getArgumentProviders().add((CommandLineArgumentProvider) () -> {
            LinkedList<String> args = new LinkedList<>();

            if (getProjectDirectory().isPresent()) {
                args.add(getProjectDirectory().getAsFile().get().getAbsolutePath());
            }

            return args;
        });
    }
}
