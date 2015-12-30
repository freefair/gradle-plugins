package io.freefair.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project


public class GitVersionPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {

        if(project.hasProperty("version") && !project.properties.get("version").equals("unspecified")){
            def propertiesVersion = project.properties.get("version")
            project.logger.lifecycle("using the non-git version {}", propertiesVersion)
            project.version = propertiesVersion
            return;
        }

        try {
            def lastTag = 'git describe --abbrev=0 --tags'.execute([], project.projectDir).text.trim()
            def currentTag = 'git tag --points-at HEAD'.execute([], project.projectDir).text.trim()

            if(lastTag.equals(currentTag)){
                if(currentTag.equals("")) {
                    project.version = "-SNAPSHOT"
                    project.logger.warn("No git tag found")
                }
                else
                    project.version = currentTag
            } else {
                project.version = "$currentTag-SNAPSHOT"
            }
        } catch (Exception e){
            project.logger.error("Can't evaluate git tags", e);
        }
    }
}
