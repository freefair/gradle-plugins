package io.freefair.gradle.plugins.git;

import io.freefair.gradle.util.GitUtil;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lars Grefer
 */
public abstract class GitVersionPlugin implements Plugin<Project> {

    private static final Pattern releaseBranchPattern = Pattern.compile("release-(\\d+.*)");
    private static final Pattern hotfixBranchPattern = Pattern.compile("hotfix-(\\d+.*)");
    private static final Pattern vTagPattern = Pattern.compile("v(\\d+.*)");

    private Logger logger;

    @Inject
    protected abstract ProviderFactory getProviderFactory();

    @Override
    public void apply(Project project) {
        this.logger = project.getLogger();
        project.setVersion(resolveVersion(project));

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

    private Object resolveVersion(Project project) {
        if (!"unspecified".equals(project.getVersion().toString())) {
            logger.lifecycle("Using explicit version {}", project.getVersion());
            return project.getVersion();
        }

        if (GitUtil.isTravisCi(getProviderFactory())) {
            Provider<String> travisTag = getProviderFactory().environmentVariable("TRAVIS_TAG");
            if (travisTag.isPresent()) {
                String version = resolveTagVersion(travisTag.get().trim());
                logger.lifecycle("Using TRAVIS_TAG '{}' as version: {}", travisTag.get(), version);
                return version;
            }

            Provider<String> travisBranch = getProviderFactory().environmentVariable("TRAVIS_BRANCH");
            if (travisBranch.isPresent()) {
                String version = resolveBranchVersion(travisBranch.get());
                logger.lifecycle("Using TRAVIS_BRANCH '{}' as version: {}", travisBranch.get(), version);
                return version;
            }
        } else if (GitUtil.isGithubActions(getProviderFactory())) {
            Provider<String> githubRef = getProviderFactory().environmentVariable("GITHUB_REF");
            if (githubRef.isPresent()) {
                if (githubRef.get().startsWith("refs/tags/")) {
                    String tag = githubRef.get().substring("refs/tags/".length());
                    String version = resolveTagVersion(tag);
                    logger.lifecycle("Using GitHub Tag '{}' as version: {}", githubRef.get(), version);
                    return version;
                } else if (githubRef.get().startsWith("refs/heads/")) {
                    String branch = githubRef.get().substring("refs/heads/".length());
                    String version = resolveBranchVersion(branch);
                    logger.lifecycle("Using GitHub Branch '{}' as version: {}", githubRef.get(), version);
                    return version;
                } else if (githubRef.get().startsWith("refs/pull/")) {
                    String pullRequest = githubRef.get().substring("refs/pull/".length());
                    String version = resolveBranchVersion(pullRequest);
                    logger.lifecycle("Using GitHub Pull Request '{}' as version: {}", githubRef.get(), version);
                    return version;
                } else {
                    logger.warn("Unknown prefix for 'GITHUB_REF' {}", githubRef.get());
                }
            } else {
                logger.warn("No GITHUB_REF found on GitHub Actions");
            }
        } else if (GitUtil.isCircleCi(getProviderFactory())) {
            Provider<String> circleTag = getProviderFactory().environmentVariable("CIRCLE_TAG");
            if (circleTag.isPresent()) {
                String version = resolveTagVersion(circleTag.get().trim());
                logger.lifecycle("Using CIRCLE_TAG '{}' as version: {}", circleTag.get(), version);
                return version;
            }

            Provider<String> circleBranch = getProviderFactory().environmentVariable("CIRCLE_BRANCH");
            if (circleBranch.isPresent()) {
                String version = resolveBranchVersion(circleBranch.get());
                logger.lifecycle("Using CIRCLE_BRANCH '{}' as version: {}", circleBranch.get(), version);
                return version;
            }

        } else if (GitUtil.isGitLab(getProviderFactory())) {
            Provider<String> gitLabTag = getProviderFactory().environmentVariable("CI_COMMIT_TAG");
            if (gitLabTag.isPresent()) {
                String version = resolveTagVersion(gitLabTag.get().trim());
                logger.lifecycle("Using CI_COMMIT_TAG '{}' as version: {}", gitLabTag.get(), version);
                return version;
            }

            Provider<String> gitLabBranch = getProviderFactory().environmentVariable("CI_COMMIT_BRANCH");
            if (gitLabBranch.isPresent()) {
                String version = resolveBranchVersion(gitLabBranch.get());
                logger.lifecycle("Using CI_COMMIT_BRANCH '{}' as version: {}", gitLabBranch.get(), version);
                return version;
            }
        }

        try {
            String gitTag = getProviderFactory()
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
                return version;
            }
        } catch (Exception e) {
            logger.debug("Failed to get current git tag", e);
        }

        if (GitUtil.isJenkins(getProviderFactory())) {
            Provider<String> gitLocalBranch = getProviderFactory().environmentVariable("GIT_LOCAL_BRANCH");
            if (gitLocalBranch.isPresent()) {
                String version = resolveBranchVersion(gitLocalBranch.get());
                logger.lifecycle("Using GIT_LOCAL_BRANCH '{}' as version: {}", gitLocalBranch.get(), version);
                return version;
            }
            Provider<String> gitBranch = getProviderFactory().environmentVariable("GIT_BRANCH");
            if (gitBranch.isPresent()) {
                String version = resolveBranchVersion(gitBranch.get());
                logger.lifecycle("Using GIT_BRANCH '{}' as version: {}", gitBranch.get(), version);
                return version;
            }
            Provider<String> branchName = getProviderFactory().environmentVariable("BRANCH_NAME");
            if (branchName.isPresent()) {
                String version = resolveBranchVersion(branchName.get());
                logger.lifecycle("Using BRANCH_NAME '{}' as version: {}", branchName.get(), version);
                return version;
            }
        }

        try {
            String gitBranch = getProviderFactory()
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
