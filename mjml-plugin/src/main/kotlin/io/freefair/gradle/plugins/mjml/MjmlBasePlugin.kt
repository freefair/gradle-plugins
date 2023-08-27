package io.freefair.gradle.plugins.mjml

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.npm.task.NpmTask
import org.gradle.api.Plugin
import org.gradle.api.Project


/**
 * @author Dennis Fricke
 */
class MjmlBasePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val mjmlExtension = project.extensions.create("mjml", MjmlExtension::class.java)

        project.plugins.apply(NodePlugin::class.java)

        project.tasks.register("installMjml", NpmTask::class.java) {
            workingDir.set(project.layout.buildDirectory.asFile.get())
            args.set(listOf("install", "mjml"))
        }

        project.tasks.withType(MjmlCompile::class.java)
            .configureEach {
                this.dependsOn("installMjml")
                this.workingDir.set(project.layout.buildDirectory)
                this.validationLevel.convention(mjmlExtension.validationMode.map { m -> m.name.lowercase() })
                this.beautify.convention(mjmlExtension.beautify)
                this.minify.convention(mjmlExtension.minify)
                this.minifyOptions.convention(mjmlExtension.minifyOptions)
                this.juiceOptions.convention(mjmlExtension.juiceOptions)
                this.juicePreserveTags.convention(mjmlExtension.juicePreserveTags)
            }
    }
}
