package io.freefair.gradle.plugins.github;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.Project;
import org.gradle.process.ExecResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
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
    public static String findSlug(Project project) throws UnsupportedEncodingException {

        Optional<String> travisSlug = findTravisSlug(project);

        if (travisSlug.isPresent()) {
            return travisSlug.get();
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

    @Nonnull
    public Optional<String> findTravisSlug(Project project) throws UnsupportedEncodingException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ExecResult travisSlugResult = project.exec(execSpec -> {
            execSpec.workingDir(project.getProjectDir());
            execSpec.commandLine("git", "config", "travis.slug");
            execSpec.setStandardOutput(outputStream);
            execSpec.setIgnoreExitValue(true);
        });

        if (travisSlugResult.getExitValue() == 0) {
            return Optional.of(outputStream.toString("UTF-8").trim());
        }
        return Optional.empty();
    }

    public String getRemoteUrl(Project project, String remote) throws UnsupportedEncodingException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ExecResult execResult = project.exec(execSpec -> {
            execSpec.workingDir(project.getProjectDir());
            execSpec.commandLine("git", "ls-remote", "--get-url", remote);
            execSpec.setStandardOutput(outputStream);
        });

        execResult.rethrowFailure().assertNormalExitValue();

        return outputStream.toString("UTF-8").trim();
    }
}
