package io.freefair.gradle.plugins.mjml

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.npm.exec.NpmExecRunner
import com.github.gradle.node.npm.task.NpxTask
import com.github.gradle.node.util.DefaultProjectApiHelper
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance
import org.gradle.process.ExecResult
import javax.inject.Inject


@CacheableTask
abstract class MjmlCompile : SourceTask() {
    @get:Inject
    abstract val objects: ObjectFactory

    @get:Internal
    val projectHelper = project.objects.newInstance<DefaultProjectApiHelper>()

    @get:Internal
    val mjmlExtension = project.extensions.getByType(MjmlExtension::class.java)

    @get:Internal
    val nodeExtension = project.extensions.getByType(NodeExtension::class.java)

    @get:Internal
    val buildDirectory = project.layout.buildDirectory.asFile.get().absoluteFile

    init {
        include("**/*.mjml")
    }

    @OutputFiles
    protected fun getOutputFiles(): FileTree? {
        val files = project.fileTree(getDestinationDir())
        files.include("**/*.html")
        return files
    }

    @Internal
    abstract fun getDestinationDir(): DirectoryProperty

    @TaskAction
    fun compileMjml() {
        source.files.forEach { file ->
            val outputFile = getDestinationDir().file(file.name.replace(".mjml", ".html"))
            val fullCommand: MutableList<String> = mutableListOf("mjml", file.absolutePath, "-o", outputFile.get().asFile.absolutePath, *buildArgs())
            val nodeExecConfiguration = NodeExecConfiguration(fullCommand, emptyMap(), buildDirectory, false, null)
            val npmExecRunner = objects.newInstance(NpmExecRunner::class.java)
            val result = npmExecRunner.executeNpxCommand(projectHelper, nodeExtension, nodeExecConfiguration, VariantComputer())
            if(result.exitValue != 0) throw Exception("Mjml failed")
        }
    }


    private fun buildArgs(): Array<out String> {
        val result = mutableListOf<String>()
        result.add("-l", mjmlExtension.validationMode.get().name.lowercase())
        if(mjmlExtension.minify.get()) result.add("--config.minify", "true")
        if(mjmlExtension.beautify.get()) result.add("--config.beautify", "true")
        if(mjmlExtension.minifyOptions.get().isNotBlank()) result.add("--config.minifyOptions", mjmlExtension.minifyOptions.get())
        if(mjmlExtension.juiceOptions.get().isNotBlank()) result.add("--config.juiceOptions", mjmlExtension.juiceOptions.get())
        if(mjmlExtension.juicePreserveTags.get().isNotBlank()) result.add("--config.juicePreserveTags", mjmlExtension.juicePreserveTags.get())
        return result.toTypedArray()
    }

    private fun MutableList<String>.add(vararg args: String) = addAll(args)
}