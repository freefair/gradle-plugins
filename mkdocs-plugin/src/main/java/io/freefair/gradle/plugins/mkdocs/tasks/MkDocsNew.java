package io.freefair.gradle.plugins.mkdocs.tasks;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.process.ExecSpec;

/**
 * Create a new MkDocs project
 */
public abstract class MkDocsNew extends MkDocs {

    @Optional
    @OutputDirectory
    public abstract DirectoryProperty getProjectDirectory();

    public MkDocsNew() {
        super("new");
        setDescription("Create a new MkDocs project");
    }

    @Override
    void setArgs(ExecSpec mkdocs) {
        if (getProjectDirectory().isPresent()) {
            mkdocs.args(getProjectDirectory().getAsFile().get().getAbsolutePath());
        }
    }
}
