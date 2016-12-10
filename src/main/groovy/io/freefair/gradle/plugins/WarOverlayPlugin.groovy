package io.freefair.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.War

/**
 * @author Lars Grefer
 */
@SuppressWarnings("GroovyUnusedDeclaration")
class WarOverlayPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply(WarPlugin)

        def overlayConfiguration = project.configurations.create("overlay")

        project.configurations.getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME).extendsFrom(overlayConfiguration)

        def explodedOverlayTask = project.tasks.create("explodedOverlay")
        explodedOverlayTask.group = "overlay"

        project.afterEvaluate {
            overlayConfiguration.resolvedConfiguration.firstLevelModuleDependencies.each { ResolvedDependency resolvedDependency ->
                resolvedDependency.moduleArtifacts.each { ResolvedArtifact resolvedArtifact ->
                    project.tasks.withType(War) { War w ->
                        w.from(project.zipTree(resolvedArtifact.file)) {
                            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                            exclude "WEB-INF/lib/*.jar"
                        }
                        w.rootSpec.exclude("**/${resolvedArtifact.file.name}")
                    }

                    Sync overlaySyncTask = project.tasks.create("exploded${resolvedArtifact.name.capitalize()}", Sync)
                    explodedOverlayTask.dependsOn overlaySyncTask
                    overlaySyncTask.group = "overlay"
                    overlaySyncTask.from(project.zipTree(resolvedArtifact.file))
                    overlaySyncTask.into(project.file("$project.buildDir/overlay/exploded/${resolvedArtifact.file.name - ".war"}"))
                }
            }
        }
    }
}
