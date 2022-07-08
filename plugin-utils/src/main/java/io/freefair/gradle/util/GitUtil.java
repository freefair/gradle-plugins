package io.freefair.gradle.util;

import lombok.experimental.UtilityClass;
import org.codehaus.groovy.runtime.ProcessGroovyMethods;
import org.gradle.api.Project;
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

        return execute(project, "git", "rev-parse", "HEAD");
    }

    public String getRef(Project project) {
        if (isGithubActions()) {
            return System.getenv("GITHUB_REF");
        }

        return execute(project, "git", "symbolic-ref", "HEAD");
    }

    public static String execute(Project project, String... command) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ExecResult execResult = project.exec(execSpec -> {
            execSpec.workingDir(project.getProjectDir());
            execSpec.commandLine((Object[]) command);
            execSpec.setStandardOutput(outputStream);
        });

        if (execResult.getExitValue() == 0) {
            return outputStream.toString().trim();
        } else {
            return null;
        }
    }
}
