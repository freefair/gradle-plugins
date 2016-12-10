package io.freefair.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.War

@SuppressWarnings("GroovyUnusedDeclaration")
class ExplodedWarPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {

        project.tasks.withType(War) { War war ->
            Sync explodedWarTask = project.tasks.create("exploded${war.name.capitalize()}", Sync)
            explodedWarTask.dependsOn war
            explodedWarTask.group = war.group
            explodedWarTask.from(project.zipTree(war.getArchivePath()))
            explodedWarTask.into(project.file("${war.getDestinationDir()}/exploded/${(war.getArchiveName() - ".war")}"))

        }
    }
}
