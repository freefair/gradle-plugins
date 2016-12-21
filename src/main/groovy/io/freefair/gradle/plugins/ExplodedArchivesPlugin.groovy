package io.freefair.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.AbstractArchiveTask

/**
 * @author Lars Grefer
 */
@SuppressWarnings("GroovyUnusedDeclaration")
class ExplodedArchivesPlugin implements Plugin<Project> {

    private ExplodedArchivesExtension extension

    @Override
    void apply(Project project) {

        extension = project.extensions.create("explodedArchives", ExplodedArchivesExtension)

        project.afterEvaluate {

            def explodedArchivesTask = project.tasks.create("explode")

            explodedArchivesTask.group = BasePlugin.BUILD_GROUP

            project.tasks.withType(AbstractArchiveTask) { AbstractArchiveTask aat ->
                Sync explodedArchiveTask = project.tasks.create("explode${aat.name.capitalize()}", Sync)

                explodedArchivesTask.dependsOn explodedArchiveTask
                explodedArchiveTask.dependsOn aat

                explodedArchiveTask.group = aat.group
                explodedArchiveTask.destinationDir = project.file({
                    project.file("${aat.destinationDir}/exploded")
                })
                explodedArchiveTask.from({ project.zipTree(aat.getArchivePath()) })
                explodedArchiveTask.into({
                    extension.includeExtension ? aat.archiveName : aat.archiveName - ".$aat.extension"
                })

            }
        }
    }
}
