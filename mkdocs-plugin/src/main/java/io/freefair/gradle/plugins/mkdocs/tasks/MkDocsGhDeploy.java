package io.freefair.gradle.plugins.mkdocs.tasks;

import lombok.Getter;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecSpec;

import javax.inject.Inject;

/**
 * Deploy your documentation to GitHub Pages.
 */
@Getter
public class MkDocsGhDeploy extends MkDocs {

    /**
     * Provide a specific MkDocs config.
     */
    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    private final RegularFileProperty configFile = getProject().getObjects().fileProperty();

    /**
     * A commit message to use when committing to the Github Pages remote branch.
     * Commit {sha} and MkDocs {version} are available as expansions.
     */
    @Optional
    @Input
    private final Property<String> message = getProject().getObjects().property(String.class);

    /**
     * The remote branch to commit to for Github Pages.
     * This overrides the value specified in config.
     */
    @Optional
    @Input
    private final Property<String> remoteBranch = getProject().getObjects().property(String.class);

    /**
     * The remote name to commit to for Github Pages.
     * This overrides the value specified in config.
     */
    @Optional
    @Input
    private final Property<String> remoteName = getProject().getObjects().property(String.class);

    /**
     * Force the push to the repository.
     */
    @Optional
    @Input
    private final Property<Boolean> force = getProject().getObjects().property(Boolean.class);

    /**
     * Ignore check that build is not being deployed
     * with an older version of MkDocs.
     */
    @Optional
    @Input
    private final Property<Boolean> ignoreVersion = getProject().getObjects().property(Boolean.class);

    @Inject
    public MkDocsGhDeploy(ExecOperations execOperations) {
        super(execOperations, "gh-deploy");
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
