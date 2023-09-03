package io.freefair.gradle.plugins.git;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import io.freefair.gradle.util.GitUtil;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lars Grefer
 */
public class GitVersionPlugin implements Plugin<Project> {

    private static final Pattern releaseBranchPattern = Pattern.compile("release-(\\d+.*)");
    private static final Pattern hotfixBranchPattern = Pattern.compile("hotfix-(\\d+.*)");
    private static final Pattern vTagPattern = Pattern.compile("v(\\d+.*)");

    private Project project;
    private Logger logger;

    @Override
    public void apply(Project project) {
        this.project = project;
        this.logger = project.getLogger();
        project.setVersion(resolveVersion());

        project.allprojects(p -> p.setVersion(project.getVersion()));
    }

    protected String resolveBranchVersion(String branch) {
        String baseVersion = branch;

        Matcher matcher = releaseBranchPattern.matcher(branch);
        if (matcher.matches()) {
            baseVersion = matcher.group(1);
        }

        matcher = hotfixBranchPattern.matcher(branch);
        if (matcher.matches()) {
            baseVersion = matcher.group(1);
        }

        baseVersion = baseVersion.replace("/", "-");

        return baseVersion + "-SNAPSHOT";
    }

    protected String resolveTagVersion(String tag) {
        String version = tag;

        Matcher matcher = vTagPattern.matcher(tag);
        if (matcher.matches()) {
            version = matcher.group(1);
        }

        return version;
    }

    private Object resolveVersion() {
        if (!"unspecified".equals(project.getVersion().toString())) {
            logger.lifecycle("Using explicit version {}", project.getVersion());
            return project.getVersion();
        }

        ProviderFactory providerFactory = project.getProviders();

        if (GitUtil.isTravisCi(providerFactory)) {
            Provider<String> travisTag = providerFactory.environmentVariable("TRAVIS_TAG");
            if (travisTag.isPresent()) {
                String version = resolveTagVersion(travisTag.get().trim());
                logger.lifecycle("Using TRAVIS_TAG '{}' as version: {}", travisTag, version);
                return version;
            }

            Provider<String> travisBranch = providerFactory.environmentVariable("TRAVIS_BRANCH");
            if (travisBranch.isPresent()) {
                String version = resolveBranchVersion(travisBranch.get());
                logger.lifecycle("Using TRAVIS_BRANCH '{}' as version: {}", travisBranch, version);
                return version;
            }
        }
        else if (GitUtil.isGithubActions(providerFactory)) {
            Provider<String> githubRef = providerFactory.environmentVariable("GITHUB_REF");
            if (githubRef.isPresent()) {
                if (githubRef.get().startsWith("refs/tags/")) {
                    String tag = githubRef.get().substring("refs/tags/".length());
                    String version = resolveTagVersion(tag);
                    logger.lifecycle("Using GitHub Tag '{}' as version: {}", githubRef, version);
                    return version;
                }
                else if (githubRef.get().startsWith("refs/heads/")) {
                    String branch = githubRef.get().substring("refs/heads/".length());
                    String version = resolveBranchVersion(branch);
                    logger.lifecycle("Using GitHub Branch '{}' as version: {}", githubRef, version);
                    return version;
                }
                else {
                    logger.warn("Unknown prefix for 'GITHUB_REF' {}", githubRef.get());
                }
            }
            else {
                logger.warn("No GITHUB_REF found on GitHub Actions");
            }
        }
        else if (GitUtil.isCircleCi(providerFactory)) {
            Provider<String> circleTag = providerFactory.environmentVariable("CIRCLE_TAG");
            if (circleTag.isPresent()) {
                String version = resolveTagVersion(circleTag.get().trim());
                logger.lifecycle("Using CIRCLE_TAG '{}' as version: {}", circleTag, version);
                return version;
            }

            Provider<String> circleBranch = providerFactory.environmentVariable("CIRCLE_BRANCH");
            if (circleBranch.isPresent()) {
                String version = resolveBranchVersion(circleBranch.get());
                logger.lifecycle("Using CIRCLE_BRANCH '{}' as version: {}", circleBranch, version);
                return version;
            }

        } else if (GitUtil.isGitLab(providerFactory)) {
            Provider<String> gitLabTag = providerFactory.environmentVariable("CI_COMMIT_TAG");
            if (gitLabTag.isPresent()) {
                String version = resolveTagVersion(gitLabTag.get().trim());
                logger.lifecycle("Using CI_COMMIT_TAG '{}' as version: {}", gitLabTag, version);
                return version;
            }

            Provider<String> gitLabBranch = providerFactory.environmentVariable("CI_COMMIT_BRANCH");
            if (gitLabBranch.isPresent()) {
                String version = resolveBranchVersion(gitLabBranch.get());
                logger.lifecycle("Using CI_COMMIT_BRANCH '{}' as version: {}", gitLabBranch, version);
                return version;
            }
        }

        try {
            String gitTag = providerFactory
                    .exec(execSpec -> {
                        execSpec.setWorkingDir(project.getProjectDir());
                        execSpec.commandLine("git", "describe", "--tags", "--exact-match", "--dirty=-SNAPSHOT");
                        execSpec.setIgnoreExitValue(true);
                    })
                    .getStandardOutput()
                    .getAsText()
                    .get()
                    .trim();

            if (!gitTag.isEmpty()) {
                String version = resolveTagVersion(gitTag);
                logger.lifecycle("Using git tag '{}' as version: {}", gitTag, version);
                return gitTag;
            }
        } catch (Exception e) {
            logger.debug("Failed to get current git tag", e);
        }

        if (GitUtil.isJenkins(providerFactory)) {
            Provider<String> gitLocalBranch = providerFactory.environmentVariable("GIT_LOCAL_BRANCH");
            if (gitLocalBranch.isPresent()) {
                String version = resolveBranchVersion(gitLocalBranch.get());
                logger.lifecycle("Using GIT_LOCAL_BRANCH '{}' as version: {}", gitLocalBranch, version);
                return version;
            }
            Provider<String> gitBranch = providerFactory.environmentVariable("GIT_BRANCH");
            if (gitBranch.isPresent()) {
                String version = resolveBranchVersion(gitBranch.get());
                logger.lifecycle("Using GIT_BRANCH '{}' as version: {}", gitBranch, version);
                return version;
            }
            Provider<String> branchName = providerFactory.environmentVariable("BRANCH_NAME");
            if (branchName.isPresent()) {
                String version = resolveBranchVersion(branchName.get());
                logger.lifecycle("Using BRANCH_NAME '{}' as version: {}", branchName, version);
                return version;
            }
        }

        try {
            String gitBranch = providerFactory
                    .exec(execSpec -> {
                        execSpec.setWorkingDir(project.getProjectDir());
                        execSpec.commandLine("git", "symbolic-ref", "--short", "HEAD");
                    })
                    .getStandardOutput()
                    .getAsText()
                    .get()
                    .trim();

            if (!gitBranch.isEmpty()) {
                String version = resolveBranchVersion(gitBranch);
                logger.lifecycle("Using git branch '{}' as version: {}", gitBranch, version);
                return version;
            }
        } catch (Exception e) {
            logger.debug("Failed to get current git branch", e);
        }

        logger.warn("Failed to infer the project version from git. Keeping {}", project.getVersion());
        return project.getVersion();
    }

}
