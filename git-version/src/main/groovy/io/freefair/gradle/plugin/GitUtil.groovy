package io.freefair.gradle.plugin

import org.gradle.api.Project

class GitUtil {

    private Project project;
    private def log;

    public GitUtil(Project project) {
        this.project = project;
        log = project.logger
    }

    public List<String> getCurrentTags(String prefix) {
        return getTagsPointingAt("HEAD", prefix)
    }

    public List<String> getLastTags(String prefix) {
        def cmd = "git describe --abbrev=0 --tags --match $prefix*"
        String lastTag = exec(cmd)
        return getTagsPointingAt(lastTag, prefix)
    }

    public List<String> getTagsPointingAt(String refspec, String prefix) {
        def cmd = "git tag --points-at ${refspec} -l $prefix*"
        String trim = exec(cmd);
        return trim.readLines()
    }

    private String exec(String command) {
        log.info("Executing: '{}'", command);

        try {
            String output = command.execute([], project.getProjectDir()).text.trim()
            log.info("Got: '{}'", output)
            return output;
        } catch (Exception e) {
            log.error("Failed to execute: '{}'", command)
            log.error(e.getMessage())
            throw e;
        }
    }
}
