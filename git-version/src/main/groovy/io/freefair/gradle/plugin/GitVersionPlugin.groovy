package io.freefair.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

public class GitVersionPlugin implements Plugin<Project> {

    Project project;
    GitUtil gitUtil;
    GitVersionConvention convention;

    @Override
    void apply(Project project) {
        this.project = project;

        gitUtil = new GitUtil(project);

        project.convention.plugins.put("gitVersion", new GitVersionConvention())
        convention = project.getConvention().getPlugin(GitVersionConvention)

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
        List<String> currentTags = gitUtil.getCurrentTags(convention.gitTagPrefix);

        if (currentTags.isEmpty()) {
            List<String> lastTags = gitUtil.getLastTags(convention.gitTagPrefix);

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
        if (tag.startsWith(convention.gitTagPrefix)) {
            return tag.substring(convention.gitTagPrefix.length());
        } else {
            project.logger.warn("Internal Error, Tag {} doesn't match prefix {}", tag, convention.gitTagPrefix);
            return tag;
        }
    }

    private String getVersion(List<String> tagList) {
        String tag = tagList.toSorted(convention.gitVersionComparator).last();
        return getVersion(tag);
    }

}
