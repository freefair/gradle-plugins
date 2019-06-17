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

        try {
            Process execute = ProcessGroovyMethods.execute("git symbolic-ref --short HEAD");
            String gitBranch = ProcessGroovyMethods.getText(execute).trim();

            if (!gitBranch.isEmpty()) {
                gitBranch = gitBranch.replace("/", "-");
                String version = gitBranch + "-SNAPSHOT";
                logger.lifecycle("Using git branch as version: {}", version);
                return version;
            }
        } catch (Exception ignored) {
        }

        return project.getVersion();
    }
}
