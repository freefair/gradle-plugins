package io.freefair.gradle.plugins.git;

import org.codehaus.groovy.runtime.ProcessGroovyMethods;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

public class GitVersionPlugin implements Plugin<Project> {

    private Project project;
    private Logger logger;

    @Override
    public void apply(Project project) {
        this.project = project;
        this.logger = project.getLogger();
        project.setVersion(resolveVersion());

        project.allprojects(p -> p.setVersion(project.getVersion()));
    }

    private Object resolveVersion() {
        if (!"unspecified".equals(project.getVersion().toString())) {
            logger.lifecycle("Using explicit version {}", project.getVersion());
            return project.getVersion();
        }

        String travisTag = System.getenv("TRAVIS_TAG");
        if (travisTag != null && !travisTag.trim().isEmpty()) {
            logger.lifecycle("Using TRAVIS_TAG as version: {}", travisTag);
            return travisTag;
        }

        String travisBranch = System.getenv("TRAVIS_BRANCH");
        if (travisBranch != null) {
            travisBranch = travisBranch.replace("/", "-");
            String version = travisBranch + "-SNAPSHOT";
            logger.lifecycle("Using TRAVIS_BRANCH as version: {}", version);
            return version;
        }

        if ("true".equalsIgnoreCase(System.getenv("GITHUB_ACTIONS"))) {
            String githubRef = System.getenv("GITHUB_REF");
            if (githubRef != null) {
                if (githubRef.startsWith("refs/tags/")) {
                    githubRef = githubRef.substring("refs/tags/".length());
                    logger.lifecycle("Using GitHub Tag as version: {}", githubRef);
                    return githubRef;
                }
                else if (githubRef.startsWith("refs/heads/")) {
                    githubRef = githubRef.substring("refs/heads/".length());
                    githubRef = githubRef.replace("/", "-");
                    String version = githubRef + "-SNAPSHOT";
                    logger.lifecycle("Using GitHub Branch as version: {}", version);
                    return version;
                }
            }
        }

        try {
            Process execute = ProcessGroovyMethods.execute("git describe --tags --exact-match");
            String gitTag = ProcessGroovyMethods.getText(execute).trim();

            if (!gitTag.isEmpty()) {
                logger.lifecycle("Using git tag as version: {}", gitTag);
                return gitTag;
            }
        } catch (Exception e) {
            logger.debug("Failed to get current git tag", e);
        }

        if (System.getenv("JENKINS_HOME") != null) {
            String gitLocalBranch = System.getenv("GIT_LOCAL_BRANCH");
            if (gitLocalBranch != null && !gitLocalBranch.isEmpty()) {
                gitLocalBranch = gitLocalBranch.replace("/", "-");
                String version = gitLocalBranch + "-SNAPSHOT";
                logger.lifecycle("Using GIT_LOCAL_BRANCH as version: {}", version);
                return version;
            }
            String gitBranch = System.getenv("GIT_BRANCH");
            if (gitBranch != null && !gitBranch.isEmpty()) {
                gitBranch = gitBranch.replace("/", "-");
                String version = gitBranch + "-SNAPSHOT";
                logger.lifecycle("Using GIT_BRANCH as version: {}", version);
                return version;
            }
            String branchName = System.getenv("BRANCH_NAME");
            if (branchName != null && !branchName.isEmpty()) {
                branchName = branchName.replace("/", "-");
                String version = branchName + "-SNAPSHOT";
                logger.lifecycle("Using BRANCH_NAME as version: {}", version);
                return version;
            }
        }

        try {
            Process execute = ProcessGroovyMethods.execute("git symbolic-ref --short HEAD");
            String gitBranch = ProcessGroovyMethods.getText(execute).trim();

            if (!gitBranch.isEmpty()) {
                gitBranch = gitBranch.replace("/", "-");
                String version = gitBranch + "-SNAPSHOT";
                logger.lifecycle("Using git branch as version: {}", version);
                return version;
            }
        } catch (Exception e) {
            logger.debug("Failed to get current git branch", e);
        }

        return project.getVersion();
    }
}
