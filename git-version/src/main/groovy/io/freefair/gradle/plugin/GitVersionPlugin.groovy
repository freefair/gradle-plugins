package io.freefair.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project


public class GitVersionPlugin implements Plugin<Project> {

    GitVersionExtension extension;
    Project project;
    GitUtil gitUtil;

    @Override
    void apply(Project project) {
        this.project = project;
        extension = project.extensions.create("gitVersion", GitVersionExtension.class);
        gitUtil = new GitUtil(project);

        if (!checkForOtherVersion())
            useGitVersion()
    }

    private boolean checkForOtherVersion() {
        if (project.hasProperty("version") && !project.properties.get("version").equals("unspecified")) {
            def propertiesVersion = project.properties.get("version")
            project.logger.lifecycle("using the non-git version {}", propertiesVersion)
            project.version = propertiesVersion
            return true;
        }
        return false;
    }

    private void useGitVersion() {
        project.logger.debug("useGitVersion")
        List<String> currentTags = gitUtil.getCurrentTags(extension.tagPrefix);

        if (currentTags.isEmpty()) {
            List<String> lastTags = gitUtil.getLastTags(extension.tagPrefix);

            if (lastTags.isEmpty()) {
                project.version = "-SNAPSHOT"
                project.logger.warn "No git tag found"
            } else {
                project.version = "${getVersion(lastTags)}-SNAPSHOT"
            }
        } else {
            project.version = getVersion(currentTags);
        }
    }


    private String getVersion(String tag) {
        if (tag.startsWith(extension.tagPrefix)) {
            return tag.substring(extension.tagPrefix.length());
        } else {
            project.logger.warn("Internal Error, Tag {} doesn't match prefix {}", tag, extension.tagPrefix);
            return tag;
        }
    }

    private String getVersion(List<String> tagList) {
        String tag = tagList.toSorted(extension.versionComparator).last();
        return getVersion(tag);
    }

}
