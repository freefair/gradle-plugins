package io.freefair.gradle.plugins.mjml

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.npm.exec.NpmExecRunner
import com.github.gradle.node.util.DefaultProjectApiHelper
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject


@CacheableTask
abstract class MjmlCompile : SourceTask() {

    @get:Inject
    abstract val objects: ObjectFactory

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @get:Internal
    val nodeExtension: NodeExtension

    @get:Internal
    abstract val workingDir: DirectoryProperty

    init {
        include("**/*.mjml")
        nodeExtension = project.extensions.getByType(NodeExtension::class.java)
    }

    @get:OutputDirectory
    abstract val destinationDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val beautify: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val minify: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val juicePreserveTags: Property<String>

    @get:Input
    @get:Optional
    abstract val minifyOptions: Property<String>

    @get:Input
    @get:Optional
    abstract val juiceOptions: Property<String>

    @get:Input
    @get:Optional
    abstract val validationLevel: Property<String>

    @TaskAction
    fun compileMjml() {
        fileSystemOperations.delete {
            delete(destinationDir)
        }

        val projectHelper: DefaultProjectApiHelper = objects.newInstance<DefaultProjectApiHelper>()

        source.visit {
            if (!this.isDirectory && name.endsWith(".mjml")) {
                val absolutOutputFile = destinationDir.file(path.replace(".mjml", ".html")).get().asFile
                if (!absolutOutputFile.parentFile.exists()) {
                    absolutOutputFile.parentFile.mkdirs()
                }
                val fullCommand: MutableList<String> =
                    mutableListOf("mjml", file.absolutePath, "-o", absolutOutputFile.absolutePath, *buildArgs())
                val nodeExecConfiguration =
                    NodeExecConfiguration(fullCommand, emptyMap(), workingDir.get().asFile, false, null)
                val npmExecRunner = objects.newInstance(NpmExecRunner::class.java)
                val result = npmExecRunner.executeNpxCommand(
                    projectHelper,
                    nodeExtension,
                    nodeExecConfiguration,
                    VariantComputer()
                )
                if (result.exitValue != 0) throw Exception("Mjml failed")
            }
        }
    }


    private fun buildArgs(): Array<out String> {
        val result = mutableListOf<String>()
        if (validationLevel.isPresent) result.add("--config.validationLevel", validationLevel.get())
        if (minify.isPresent) result.add("--config.minify", minify.get().toString())
        if (beautify.isPresent) result.add("--config.beautify", beautify.get().toString())
        if (minifyOptions.isPresent) result.add("--config.minifyOptions", minifyOptions.get())
        if (juiceOptions.isPresent) result.add("--config.juiceOptions", juiceOptions.get())
        if (juicePreserveTags.isPresent) result.add("--config.juicePreserveTags", juicePreserveTags.get())
        return result.toTypedArray()
    }

    private fun MutableList<String>.add(vararg args: String) = addAll(args)
}
