package io.freefair.gradle.plugins.mkdocs.tasks;

import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Console;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecSpec;

@Getter
public abstract class MkDocs extends DefaultTask {

    @Getter(AccessLevel.PROTECTED)
    @Input
    private final String command;

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

    protected MkDocs(String command) {
        this.command = command;
    }

    @TaskAction
    public void exec() {
        getProject().exec(mkdocs -> {
            mkdocs.setExecutable("mkdocs");

            mkdocs.args(command);

            if (getQuiet().getOrElse(false)) {
                mkdocs.args("--quiet");
            }

            if (getVerbose().getOrElse(false)) {
                mkdocs.args("--verbose");
            }

            setArgs(mkdocs);
        });
    }

    abstract void setArgs(ExecSpec mkdocs);
}
