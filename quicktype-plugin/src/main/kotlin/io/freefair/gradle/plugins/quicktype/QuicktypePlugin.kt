package io.freefair.gradle.plugins.quicktype

import io.freefair.gradle.plugins.quicktype.internal.DefaultQuicktypeSourceSet
import io.freefair.gradle.plugins.quicktype.internal.QuicktypeCompile
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileTreeElement
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.internal.JvmPluginsHelper
import org.gradle.api.tasks.SourceSet
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

open class QuicktypePlugin : Plugin<Project> {
    private var project: Project? = null

    override fun apply(project: Project) {
        this.project = project;
        project.pluginManager.apply("com.github.node-gradle.node")
        project.pluginManager.apply(JavaPlugin::class.java)

        val javaExtension = project.extensions.getByType(JavaPluginExtension::class.java)
        javaExtension.sourceSets.all { configureSourceSet(it) }

        try {
            val kotlinExtension = project.extensions.getByType(KotlinProjectExtension::class.java)
            //kotlinExtension.sourceSets.all { configureSourceSetKotlin(it) }
        } catch (ex: Exception) {

        }
    }

    /*private fun configureSourceSetKotlin(sourceSet: KotlinSourceSet) {
        val quicktypeSourceSet = DslObject(sourceSet).extensions.create (
            "quicktype",
            DefaultQuicktypeSourceSet::class.java,
            project?.objects,
            sourceSet
        )

        quicktypeSourceSet.quicktype.srcDir("src/" + sourceSet.name + "/quicktype")
        sourceSet.resources.filter.exclude { element: FileTreeElement ->
            quicktypeSourceSet.quicktype.contains(element.file)
        }
        sourceSet.kotlin.source(quicktypeSourceSet.quicktype)

        val sourceSetPrefix = if (sourceSet.name == "main") "" else sourceSet.name

        val quicktype = project?.configurations?.create(sourceSetPrefix + quicktypeSourceSet.quicktypeConfigurationName + "Kotlin")
        quicktypeSourceSet.quicktypePath = quicktype

        val inpath = project?.configurations?.create(sourceSetPrefix + quicktypeSourceSet.inpathConfigurationName + "Kotlin")
        quicktypeSourceSet.inPath = inpath

        project?.configurations?.getByName(sourceSet.implementationConfigurationName)?.extendsFrom(quicktype)

        project?.configurations?.getByName(sourceSet.compileOnlyConfigurationName)?.extendsFrom(inpath)

        val compileTask = project?.tasks?.register(
            "kotlinQuicktypeGenerate",
            QuicktypeCompile::class.java
        ) {
            JvmPluginsHelper.configureForSourceSet(
                sourceSet,
                quicktypeSourceSet.quicktype,
                it,
                it.options,
                project
            )
            it.dependsOn(sourceSet)
            it.description = "Compiles the " + sourceSet.name + " quicktype source."
            it.source = quicktypeSourceSet.quicktype
        }

        JvmPluginsHelper.configureOutputDirectoryForSourceSet(
            sourceSet,
            quicktypeSourceSet.quicktype,
            project,
            compileTask,
            compileTask?.map { c -> c.options }
        )

        project?.tasks?.named(sourceSet) { task: Task ->
            task.dependsOn(
                compileTask
            )
        }
    }*/

    private fun configureSourceSet(sourceSet: SourceSet) {
        val quicktypeSourceSet = DslObject(sourceSet).extensions.create(
            "quicktype",
            DefaultQuicktypeSourceSet::class.java,
            project?.objects!!,
            sourceSet
        )

        quicktypeSourceSet.quicktype.srcDir("src/" + sourceSet.name + "/quicktype")
        sourceSet.resources.filter.exclude { element: FileTreeElement ->
            quicktypeSourceSet.quicktype.contains(element.file)
        }
        sourceSet.allJava.source(quicktypeSourceSet.quicktype)
        sourceSet.allSource.source(quicktypeSourceSet.quicktype)

        val sourceSetPrefix = if (sourceSet.name == "main") "" else sourceSet.name
        val quicktype = project?.configurations?.create(sourceSetPrefix + quicktypeSourceSet.quicktypeConfigurationName)
        quicktypeSourceSet.quicktypePath = quicktype

        val inpath = project?.configurations?.create(sourceSetPrefix + quicktypeSourceSet.inpathConfigurationName)
        quicktypeSourceSet.inPath = inpath

        project?.configurations?.getByName(sourceSet.implementationConfigurationName)?.extendsFrom(quicktype!!)

        project?.configurations?.getByName(sourceSet.compileOnlyConfigurationName)?.extendsFrom(inpath!!)

        val compileTask = project?.tasks?.register(
            sourceSet.getCompileTaskName("quicktype"),
            QuicktypeCompile::class.java
        ) {
            JvmPluginsHelper.compileAgainstJavaOutputs(it, sourceSet, project?.objects)
            JvmPluginsHelper.configureAnnotationProcessorPath(sourceSet, quicktypeSourceSet.quicktype, it.options, project)
            it.dependsOn(sourceSet.compileJavaTaskName)
            it.description = "Compiles the " + sourceSet.name + " quicktype source."
            it.source = quicktypeSourceSet.quicktype
        }

        JvmPluginsHelper.configureOutputDirectoryForSourceSet(
            sourceSet,
            quicktypeSourceSet.quicktype,
            project,
            compileTask,
            compileTask?.map { c -> c.options }
        )


        project?.tasks?.named(sourceSet.classesTaskName) { task: Task ->
            task.dependsOn(
                compileTask!!
            )
        }
    }
}
