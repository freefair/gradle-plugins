package io.freefair.gradle.plugins.mkdocs.tasks;

import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Console;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecSpec;

import javax.inject.Inject;

public abstract class MkDocs extends DefaultTask {

    @Inject
    protected abstract ExecOperations getExecOperations();

    @Getter(AccessLevel.PROTECTED)
    @Input
    private final String command;

    /**
     * Silence warnings.
     */
    @Console
    public abstract Property<Boolean> getQuiet();

    /**
     * Enable verbose output.
     */
    @Console
    public abstract Property<Boolean> getVerbose();

    protected MkDocs(String command) {
        this.command = command;
    }

    @TaskAction
    public void exec() {
        getExecOperations().exec(mkdocs -> {
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
