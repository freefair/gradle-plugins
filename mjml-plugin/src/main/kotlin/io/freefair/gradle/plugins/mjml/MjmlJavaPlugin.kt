package io.freefair.gradle.plugins.mjml

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.jvm.tasks.ProcessResources

/**
 * @author Dennis Fricke
 */
class MjmlJavaPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply(MjmlBasePlugin::class.java)

        val baseDestinationDir = target.layout.buildDirectory.dir("mjml")

        val javaExtension = target.extensions.getByType(JavaPluginExtension::class.java)
        javaExtension.sourceSets.all {
            val sourceSet = this
            val taskName = getCompileTaskName("Mjml")
            val outputDir = baseDestinationDir.get().dir(sourceSet.name)
            val mjmlCompileProvider: TaskProvider<MjmlCompile> =
                target.tasks.register(taskName, MjmlCompile::class.java) {
                    source = resources
                    getDestinationDir().set(outputDir)
                    group = org.gradle.api.plugins.BasePlugin.BUILD_GROUP
                    description = "Compile mjml files for the " + sourceSet.name + " source set"
                }
            this.resources {
                srcDir(outputDir)
            }
            target.tasks.named(processResourcesTaskName, ProcessResources::class.java)
                .configure {
                    from(mjmlCompileProvider)
                }
        }
    }
}
