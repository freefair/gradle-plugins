package io.freefair.gradle.plugins

import io.freefair.gradle.plugins.overlay.OverlayExtension
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

    OverlayExtension overlayExt;

    @Override
    void apply(Project project) {
        project.pluginManager.apply(WarPlugin)

        overlayExt = project.extensions.create("overlay", OverlayExtension)

        project.tasks.withType(War) { War w ->

            def configTask = project.tasks.create("configureOverlayFor${w.name.capitalize()}")

            w.dependsOn configTask

            configTask.doFirst {
                project.configurations.getByName("runtime").each {
                    if (it.name.endsWith(".war")) {
                        w.from(project.zipTree(it)) { CopySpec css ->
                            css.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                            css.exclude overlayExt.excludes
                        }

                        w.rootSpec.exclude "**/$it.name"
                    }
                }
            }
        }
    }
}
