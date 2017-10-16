package io.freefair.gradle.plugins;

import org.gradle.api.Incubating;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lars Grefer
 */
@Incubating
public class GitVersionPlugin implements Plugin<Project> {

    private GitUtil gitUtil;
    private GitVersionConvention convention;

    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;

        gitUtil = new GitUtil(project);

        project.getConvention().getPlugins().put("gitVersion", new GitVersionConvention());
        convention = project.getConvention().getPlugin(GitVersionConvention.class);

        if (!checkForOtherVersion()) {
            useGitVersion();
        }

    }

    private boolean checkForOtherVersion() {
        if (project.hasProperty("version") && !project.getProperties().get("version").equals("unspecified")) {
            String propertiesVersion = (String) project.getProperties().get("version");

            project.getLogger().lifecycle("using the non-git version {}", propertiesVersion);
            project.setVersion(propertiesVersion);
            return true;
        }
        return false;
    }

    private void useGitVersion() {
        project.getLogger().debug("useGitVersion");

        gitUtil.fetchTags();

        List<String> currentTags = gitUtil.getCurrentTags(convention.getGitTagPrefix());

        if (currentTags.isEmpty()) {
            List<String> lastTags = gitUtil.getLastTags(convention.getGitTagPrefix());

            if (lastTags.isEmpty()) {
                project.setVersion("-SNAPSHOT");
                project.getLogger().warn("No git tag found");
            } else {
                project.setVersion(getVersion(lastTags) + "-SNAPSHOT");
            }
        } else {
            project.setVersion(getVersion(currentTags));
        }
    }


    private String getVersion(String tag) {
        if (tag.startsWith(convention.getGitTagPrefix())) {
            return tag.substring(convention.getGitTagPrefix().length());
        } else {
            project.getLogger().warn("Internal Error, Tag {} doesn't match prefix {}", tag, convention.getGitTagPrefix());
            return tag;
        }
    }

    private String getVersion(List<String> tagList) {

        List<String> newList = new ArrayList<>(tagList);

        newList.sort(convention.getGitVersionComparator());

        String tag = newList.get(newList.size() - 1);
        return getVersion(tag);
    }

}
