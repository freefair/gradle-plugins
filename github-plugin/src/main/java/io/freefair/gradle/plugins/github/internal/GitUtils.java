package io.freefair.gradle.plugins.github.internal;

import io.freefair.gradle.util.GitUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.Project;
import org.gradle.process.ExecResult;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lars Grefer
 */
@UtilityClass
@Slf4j
public class GitUtils {

    private static final Pattern httpsUrlPattern = Pattern.compile("https://github\\.com/(.+/.+)\\.git");
    private static final Pattern sshUrlPattern = Pattern.compile("git@github.com:(.+/.+)\\.git");

    @Nullable
    public static String findSlug(Project project) {

        if (GitUtil.isGithubActions(project.getProviders())) {
            return project.getProviders().environmentVariable("GITHUB_REPOSITORY").get();
        }

        String travisSlug = findTravisSlug(project);

        if (travisSlug != null) {
            return travisSlug;
        }

        String remoteUrl = getRemoteUrl(project, "origin");

        Matcher httpsMatcher = httpsUrlPattern.matcher(remoteUrl);
        if (httpsMatcher.matches()) {
            return httpsMatcher.group(1);
        }

        Matcher sshMatcher = sshUrlPattern.matcher(remoteUrl);
        if (sshMatcher.matches()) {
            return sshMatcher.group(1);
        }

        return null;
    }

    @Nullable
    public String findTravisSlug(Project project) {

        if (GitUtil.isTravisCi(project.getProviders())) {
            return project.getProviders().environmentVariable("TRAVIS_REPO_SLUG").get();
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ExecResult travisSlugResult = project.exec(execSpec -> {
            execSpec.workingDir(project.getProjectDir());
            execSpec.commandLine("git", "config", "travis.slug");
            execSpec.setStandardOutput(outputStream);
            execSpec.setIgnoreExitValue(true);
        });

        if (travisSlugResult.getExitValue() == 0) {
            return outputStream.toString().trim();
        }
        return null;
    }

    public String getRemoteUrl(Project project, String remote) {
        return GitUtil.execute(project, "git", "ls-remote", "--get-url", remote);
    }

    public File findWorkingDirectory(Project project) {

        if (GitUtil.isTravisCi(project.getProviders())) {
            return new File(System.getenv("TRAVIS_BUILD_DIR"));
        }
        else if (GitUtil.isGithubActions(project.getProviders())) {
            return new File(System.getenv("GITHUB_WORKSPACE"));
        }

        String git = GitUtil.execute(project, "git", "rev-parse", "--show-toplevel");

        if (git != null) {
            return new File(git);
        }
        else {
            return null;
        }
    }

    public String getTag(Project project) {

        if (GitUtil.isTravisCi(project.getProviders())) {
            String travisTagEnv = project.getProviders().environmentVariable("TRAVIS_TAG").getOrNull();

            if (travisTagEnv != null && !travisTagEnv.isEmpty()) {
                return travisTagEnv;
            }
        }

        if (GitUtil.isGithubActions(project.getProviders())) {
            String githubRef = System.getenv("GITHUB_REF");
            if (githubRef != null) {
                if (githubRef.startsWith("refs/tags/")) {
                    return githubRef.substring("refs/tags/".length());
                }
            }
        }

        String gitTag = GitUtil.execute(project, "git", "tag", "--points-at", "HEAD");

        if (gitTag != null) {
            return gitTag;
        }

        return "HEAD";
    }

    @Nullable
    public String findGithubUsername(Project project) {
        if (GitUtil.isGithubActions(project.getProviders())) {
            return System.getenv("GITHUB_ACTOR");
        }

        Object githubUsername = project.findProperty("githubUsername");
        if (githubUsername != null) {
            return githubUsername.toString();
        }

        return null;
    }

    @Nullable
    public String findGithubToken(Project project) {
        String github_token = System.getenv("GITHUB_TOKEN");
        if (github_token != null) {
            return github_token;
        }

        Object githubToken = project.findProperty("githubToken");
        if (githubToken != null) {
            return githubToken.toString();
        }

        return null;
    }
}
