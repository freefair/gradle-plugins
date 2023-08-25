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
            val fullCommand: MutableList<String> = mutableListOf("mjml", file.absolutePath, "-o", outputFile.get().asFile.absolutePath)
            val nodeExecConfiguration = NodeExecConfiguration(fullCommand, emptyMap(), project.layout.buildDirectory.asFile.get().absoluteFile, false, null)
            val npmExecRunner = objects.newInstance(NpmExecRunner::class.java)
            val extension = project.extensions.getByType(NodeExtension::class.java)
            val result = npmExecRunner.executeNpxCommand(projectHelper, extension, nodeExecConfiguration, VariantComputer())
            if(result.exitValue != 0) throw Exception("Mjml failed")
        }
    }

}