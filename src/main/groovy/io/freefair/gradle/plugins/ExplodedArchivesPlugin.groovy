package io.freefair.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.War

/**
 * @author Lars Grefer
 */
@SuppressWarnings("GroovyUnusedDeclaration")
class ExplodedArchivesPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {

        project.afterEvaluate {

            def explodedArchivesTask = project.tasks.create("explodedArchives")

            explodedArchivesTask.group = BasePlugin.BUILD_GROUP;

            project.tasks.withType(AbstractArchiveTask) { AbstractArchiveTask aat ->
                Sync explodedArchiveTask = project.tasks.create("exploded${aat.name.capitalize()}", Sync)

                explodedArchivesTask.dependsOn explodedArchiveTask;
                explodedArchiveTask.dependsOn aat

                explodedArchiveTask.group = aat.group
                explodedArchiveTask.destinationDir = project.file({
                    project.file("${aat.destinationDir}/exploded")
                })
                explodedArchiveTask.from({ project.zipTree(aat.getArchivePath()) })
                explodedArchiveTask.into({ aat.archiveName - ".$aat.extension" })

            }
        }
    }
}
