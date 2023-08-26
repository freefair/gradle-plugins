package io.freefair.gradle.plugins.mjml

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.npm.task.NpmTask
import org.gradle.api.Plugin
import org.gradle.api.Project


/**
 * @author Dennis Fricke
 */
class MjmlBasePlugin : Plugin<Project> {
    var mjmlExtension: MjmlExtension? = null
        private set

    private var project: Project? = null

    override fun apply(project: Project) {
        this.project = project
        mjmlExtension = project.extensions.create("mjml", MjmlExtension::class.java)

        project.plugins.apply(NodePlugin::class.java)

        project.tasks.register("installMjml", NpmTask::class.java) {
            workingDir.set(project.layout.buildDirectory.asFile.get())
            args.set(listOf("install", "mjml"))
        }

        project.tasks.withType(MjmlCompile::class.java)
            .configureEach {
                this.dependsOn("installMjml")
            }
    }
}