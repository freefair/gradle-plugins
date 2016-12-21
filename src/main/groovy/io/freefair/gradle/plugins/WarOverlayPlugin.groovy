package io.freefair.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.tasks.bundling.War

/**
 * @author Lars Grefer
 */
@SuppressWarnings("GroovyUnusedDeclaration")
class WarOverlayPlugin implements Plugin<Project> {

    WarOverlayExtension overlayExt;

    @Override
    void apply(Project project) {
        project.pluginManager.apply(WarPlugin)

        overlayExt = project.extensions.create("warOverlay", WarOverlayExtension)

        project.tasks.withType(War) { War warTask ->

            def configTask = project.tasks.create("configureOverlayFor${warTask.name.capitalize()}")

            warTask.dependsOn configTask

            configTask.doFirst {
                project.configurations.getByName("runtime").each { File file ->
                    if (file.name.endsWith(".war")) {

                        configTask.logger.info("Using {} as overlay", file.name)

                        warTask.from(project.zipTree(file)) { CopySpec cs ->
                            cs.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                            cs.exclude overlayExt.excludes
                        }

                        warTask.rootSpec.exclude "**/$file.name"
                    }
                }
            }
        }
    }
}
