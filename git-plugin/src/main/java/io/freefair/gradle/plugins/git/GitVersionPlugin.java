package io.freefair.gradle.plugins.git;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import io.freefair.gradle.util.GitUtil;

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

        if (GitUtil.isTravisCi(project.getProviders())) {
            String travisTag = System.getenv("TRAVIS_TAG");
            if (travisTag != null && !travisTag.trim().isEmpty()) {
                String version = resolveTagVersion(travisTag);
                logger.lifecycle("Using TRAVIS_TAG '{}' as version: {}", travisTag, version);
                return version;
            }

            String travisBranch = System.getenv("TRAVIS_BRANCH");
            if (travisBranch != null) {
                String version = resolveBranchVersion(travisBranch);
                logger.lifecycle("Using TRAVIS_BRANCH '{}' as version: {}", travisBranch, version);
                return version;
            }
        }
        else if (GitUtil.isGithubActions(project.getProviders())) {
            String githubRef = System.getenv("GITHUB_REF");
            if (githubRef != null) {
                if (githubRef.startsWith("refs/tags/")) {
                    githubRef = githubRef.substring("refs/tags/".length());
                    String version = resolveTagVersion(githubRef);
                    logger.lifecycle("Using GitHub Tag '{}' as version: {}", githubRef, version);
                    return version;
                }
                else if (githubRef.startsWith("refs/heads/")) {
                    githubRef = githubRef.substring("refs/heads/".length());
                    String version = resolveBranchVersion(githubRef);
                    logger.lifecycle("Using GitHub Branch '{}' as version: {}", githubRef, version);
                    return version;
                }
            } else {
                logger.warn("No GITHUB_REF found on GitHub Actions");
            }
        }
        else if (GitUtil.isCircleCi(project.getProviders())) {
            String circleTag = System.getenv("CIRCLE_TAG");
            if (circleTag != null && !circleTag.trim().isEmpty()) {
                String version = resolveTagVersion(circleTag);
                logger.lifecycle("Using CIRCLE_TAG '{}' as version: {}", circleTag, version);
                return version;
            }

            String circleBranch = System.getenv("CIRCLE_BRANCH");
            if (circleBranch != null) {
                String version = resolveBranchVersion(circleBranch);
                logger.lifecycle("Using CIRCLE_BRANCH '{}' as version: {}", circleBranch, version);
                return version;
            }
        }

        try {
            String gitTag = project.getProviders()
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

        if (GitUtil.isJenkins()) {
            String gitLocalBranch = System.getenv("GIT_LOCAL_BRANCH");
            if (gitLocalBranch != null && !gitLocalBranch.isEmpty()) {
                String version = resolveBranchVersion(gitLocalBranch);
                logger.lifecycle("Using GIT_LOCAL_BRANCH '{}' as version: {}", gitLocalBranch, version);
                return version;
            }
            String gitBranch = System.getenv("GIT_BRANCH");
            if (gitBranch != null && !gitBranch.isEmpty()) {
                String version = resolveBranchVersion(gitBranch);
                logger.lifecycle("Using GIT_BRANCH '{}' as version: {}", gitBranch, version);
                return version;
            }
            String branchName = System.getenv("BRANCH_NAME");
            if (branchName != null && !branchName.isEmpty()) {
                String version = resolveBranchVersion(branchName);
                logger.lifecycle("Using BRANCH_NAME '{}' as version: {}", branchName, version);
                return version;
            }
        }

        try {
            String gitBranch = project.getProviders()
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

        return project.getVersion();
    }

}
