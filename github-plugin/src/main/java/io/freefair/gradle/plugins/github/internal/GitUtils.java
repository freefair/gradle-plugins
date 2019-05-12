package io.freefair.gradle.plugins.github.internal;

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

        String travisSlugEnv = System.getenv("TRAVIS_REPO_SLUG");
        if (travisSlugEnv != null) {
            return travisSlugEnv.trim();
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
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ExecResult execResult = project.exec(execSpec -> {
            execSpec.workingDir(project.getProjectDir());
            execSpec.commandLine("git", "ls-remote", "--get-url", remote);
            execSpec.setStandardOutput(outputStream);
        });

        execResult.rethrowFailure().assertNormalExitValue();

        return outputStream.toString().trim();
    }

    public File findWorkingDirectory(Project project) {

        String travisBuildDirEnv = System.getenv("TRAVIS_BUILD_DIR");
        if (travisBuildDirEnv != null) {
            return new File(travisBuildDirEnv);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ExecResult execResult = project.exec(execSpec -> {
            execSpec.workingDir(project.getProjectDir());
            execSpec.commandLine("git", "rev-parse", "--show-toplevel");
            execSpec.setStandardOutput(outputStream);
            execSpec.setIgnoreExitValue(true);
        });

        if (execResult.getExitValue() == 0) {
            return new File(outputStream.toString().trim());
        }
        else {
            return null;
        }
    }

    public String getTag(Project project) {
        String travisTagEnv = System.getenv("TRAVIS_TAG");

        if (travisTagEnv != null) {
            return travisTagEnv.trim();
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ExecResult execResult = project.exec(execSpec -> {
            execSpec.workingDir(project.getProjectDir());
            execSpec.commandLine("git", "tag", "--points-at", "HEAD");
            execSpec.setStandardOutput(outputStream);
        });

        if (execResult.getExitValue() == 0) {
            String gitTag = outputStream.toString().trim();
            if (!gitTag.isEmpty()) {
                return gitTag;
            }
        }

        return "HEAD";
    }
}
