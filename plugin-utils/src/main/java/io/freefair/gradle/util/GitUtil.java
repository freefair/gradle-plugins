package io.freefair.gradle.util;

import lombok.experimental.UtilityClass;
import org.codehaus.groovy.runtime.ProcessGroovyMethods;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.process.ExecOutput;
import org.gradle.process.ExecResult;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

@UtilityClass
public class GitUtil {

    public boolean isTravisCi(ProviderFactory providerFactory) {
        return "true".equalsIgnoreCase(providerFactory.environmentVariable("TRAVIS").getOrNull());
    }

    public boolean isCircleCi(ProviderFactory providerFactory) {
        return "true".equalsIgnoreCase(providerFactory.environmentVariable("CIRCLECI").getOrNull());
    }

    public boolean isGithubActions(ProviderFactory providerFactory) {
        return "true".equalsIgnoreCase(providerFactory.environmentVariable("GITHUB_ACTIONS").getOrNull());
    }

    public boolean isJenkins(ProviderFactory providerFactory) {
        return providerFactory.environmentVariable("JENKINS_HOME").isPresent();
    }

    public boolean isGitLab(ProviderFactory providerFactory) {
        return providerFactory.environmentVariable("GITLAB_CI").isPresent();
    }

    public Provider<String> getSha(Project project) {
        if (isGithubActions(project.getProviders())) {
            return project.getProviders().environmentVariable("GITHUB_SHA");
        }

        if (isTravisCi(project.getProviders())) {
            return project.getProviders().environmentVariable("TRAVIS_COMMIT");
        }

        if (isCircleCi(project.getProviders())) {
            return project.getProviders().environmentVariable("CIRCLE_SHA1");
        }

        return execute(project, "git", "rev-parse", "HEAD");
    }

    public Provider<String> getRef(Project project) {
        if (isGithubActions(project.getProviders())) {
            return project.getProviders().environmentVariable("GITHUB_REF");
        }

        return execute(project, "git", "symbolic-ref", "HEAD");
    }

    public static Provider<String> execute(Project project, String... command) {

        ExecOutput execOutput = project.getProviders().exec(execSpec -> {
            execSpec.setWorkingDir(project.getProjectDir());
            execSpec.commandLine((Object[]) command);
        });

        return execOutput.getStandardOutput().getAsText()
                .map(String::trim);

    }
}
