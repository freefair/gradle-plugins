package io.freefair.gradle.plugins.mkdocs.tasks;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecSpec;

/**
 * Deploy your documentation to GitHub Pages.
 */
public abstract class MkDocsGhDeploy extends MkDocs {

    /**
     * Provide a specific MkDocs config.
     */
    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getConfigFile();

    /**
     * A commit message to use when committing to the Github Pages remote branch.
     * Commit {sha} and MkDocs {version} are available as expansions.
     */
    @Optional
    @Input
    public abstract Property<String> getMessage();

    /**
     * The remote branch to commit to for Github Pages.
     * This overrides the value specified in config.
     */
    @Optional
    @Input
    public abstract Property<String> getRemoteBranch();

    /**
     * The remote name to commit to for Github Pages.
     * This overrides the value specified in config.
     */
    @Optional
    @Input
    public abstract Property<String> getRemoteName();

    /**
     * Force the push to the repository.
     */
    @Optional
    @Input
    public abstract Property<Boolean> getForce();

    /**
     * Ignore check that build is not being deployed
     * with an older version of MkDocs.
     */
    @Optional
    @Input
    public abstract Property<Boolean> getIgnoreVersion();

    public MkDocsGhDeploy() {
        super("gh-deploy");
        setDescription("Deploy your documentation to GitHub Pages");
    }

    @Override
    void setArgs(ExecSpec mkdocs) {

        if (getConfigFile().isPresent()) {
            mkdocs.args("--config-file", getConfigFile().getAsFile().get().getAbsolutePath());
        }

        if (getMessage().isPresent()) {
            mkdocs.args("--message", getMessage().get());
        }

        if (getRemoteBranch().isPresent()) {
            mkdocs.args("--remote-branch", getRemoteBranch().get());
        }

        if (getRemoteName().isPresent()) {
            mkdocs.args("--remote-name", getRemoteName().get());
        }

        if (getForce().getOrElse(false)) {
            mkdocs.args("--force");
        }

        if (getIgnoreVersion().getOrElse(false)) {
            mkdocs.args("--ignore-version");
        }
    }
}
