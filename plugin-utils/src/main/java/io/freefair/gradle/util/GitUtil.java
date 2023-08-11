package io.freefair.gradle.util;

import lombok.experimental.UtilityClass;
import org.codehaus.groovy.runtime.ProcessGroovyMethods;
import org.gradle.api.Project;
import org.gradle.process.ExecOutput;
import org.gradle.process.ExecResult;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

@UtilityClass
public class GitUtil {

    public boolean isTravisCi() {
        return "true".equalsIgnoreCase(System.getenv("TRAVIS"));
    }

    public boolean isCircleCi() {
        return "true".equalsIgnoreCase(System.getenv("CIRCLECI"));
    }

    public boolean isGithubActions() {
        return "true".equalsIgnoreCase(System.getenv("GITHUB_ACTIONS"));
    }

    public boolean isJenkins() {
        return System.getenv("JENKINS_HOME") != null;
    }

    public String getSha(Project project) {
        if (isGithubActions()) {
            return System.getenv("GITHUB_SHA");
        }

        if (isTravisCi()) {
            return System.getenv("TRAVIS_COMMIT");
        }

        if (isCircleCi()) {
            return System.getenv("CIRCLE_SHA1");
        }

        return execute(project, "git", "rev-parse", "HEAD");
    }

    public String getRef(Project project) {
        if (isGithubActions()) {
            return System.getenv("GITHUB_REF");
        }

        return execute(project, "git", "symbolic-ref", "HEAD");
    }

    public static String execute(Project project, String... command) {

        ExecOutput execOutput = project.getProviders().exec(execSpec -> {
            execSpec.setWorkingDir(project.getProjectDir());
            execSpec.commandLine((Object[]) command);
        });

        return execOutput.getStandardOutput().getAsText()
                .map(String::trim)
                .get();

    }
}
