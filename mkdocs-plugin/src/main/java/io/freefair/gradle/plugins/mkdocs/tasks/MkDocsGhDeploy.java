package io.freefair.gradle.plugins.mkdocs.tasks;

import lombok.Getter;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.CommandLineArgumentProvider;

import java.util.LinkedList;

/**
 * Deploy your documentation to GitHub Pages.
 */
@SuppressWarnings("UnstableApiUsage")
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

    public MkDocsGhDeploy() {
        super("gh-deploy");
        setDescription("Deploy your documentation to GitHub Pages");

        getArgumentProviders().add((CommandLineArgumentProvider) () -> {
            LinkedList<String> args = new LinkedList<>();

            if (getConfigFile().isPresent()) {
                args.add("--config-file");
                args.add(getConfigFile().getAsFile().get().getAbsolutePath());
            }

            if (getMessage().isPresent()) {
                args.add("--message");
                args.add(getMessage().get());
            }

            if (getRemoteBranch().isPresent()) {
                args.add("--remote-branch");
                args.add(getRemoteBranch().get());
            }

            if (getRemoteName().isPresent()) {
                args.add("--remote-name");
                args.add(getRemoteName().get());
            }

            if (getForce().getOrElse(false)) {
                args.add("--force");
            }

            if (getIgnoreVersion().getOrElse(false)) {
                args.add("--ignore-version");
            }

            return args;
        });
    }
}
