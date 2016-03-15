package io.freefair.gradle.plugin

import org.codehaus.groovy.runtime.NumberAwareComparator
import org.gradle.api.Plugin
import org.gradle.api.Project


public class GitVersionPlugin implements Plugin<Project> {

    Project project;
    GitUtil gitUtil;

    String tagPrefix = '';

    Comparator<String> versionComparator = new NumberAwareComparator<>();

    @Override
    void apply(Project project) {
        this.project = project;
        gitUtil = new GitUtil(project);

        if(project.hasProperty("gitTagPrefix")) {
            tagPrefix = project.property("gitTagPrefix")
        }

        if(project.hasProperty("gitVersionComparator")) {
            versionComparator = project.property("gitVersionComparator") as Comparator<String>
        }

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
        List<String> currentTags = gitUtil.getCurrentTags(tagPrefix);

        if (currentTags.isEmpty()) {
            List<String> lastTags = gitUtil.getLastTags(tagPrefix);

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
        if (tag.startsWith(tagPrefix)) {
            return tag.substring(tagPrefix.length());
        } else {
            project.logger.warn("Internal Error, Tag {} doesn't match prefix {}", tag, tagPrefix);
            return tag;
        }
    }

    private String getVersion(List<String> tagList) {
        String tag = tagList.toSorted(versionComparator).last();
        return getVersion(tag);
    }

}
