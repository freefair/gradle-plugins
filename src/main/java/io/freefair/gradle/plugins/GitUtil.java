package io.freefair.gradle.plugins;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.util.List;

import static org.codehaus.groovy.runtime.ProcessGroovyMethods.getText;
import static org.codehaus.groovy.runtime.StringGroovyMethods.readLines;

/**
 * @author Lars Grefer
 */
class GitUtil {

    private final Project project;
    private final Logger log;

    GitUtil(Project project) {
        this.project = project;
        log = project.getLogger();
    }

    List<String> getCurrentTags(String prefix) {
        return getTagsPointingAt("HEAD", prefix);
    }

    List<String> getLastTags(String prefix) {
        String cmd = "git describe --abbrev=0 --tags --match " + prefix + "*";
        String lastTag = exec(cmd);
        return getTagsPointingAt(lastTag, prefix);
    }

    private List<String> getTagsPointingAt(String refspec, String prefix) {
        String cmd = "git tag --points-at " + refspec + " -l " + prefix + "*";
        String trim = exec(cmd);
        return readLines((CharSequence)trim);
    }

    private String exec(String command) {
        log.info("Executing: '{}'", command);

        try {
            Process gitProcess = Runtime.getRuntime().exec(command, null, project.getProjectDir());
            String output = getText(gitProcess).trim();

            log.info("Got: '{}'", output);
            return output;
        } catch (Exception e) {
            log.error("Failed to execute: '{}'", command);
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    void fetchTags() {
        exec("git fetch --all -v");
    }
}
