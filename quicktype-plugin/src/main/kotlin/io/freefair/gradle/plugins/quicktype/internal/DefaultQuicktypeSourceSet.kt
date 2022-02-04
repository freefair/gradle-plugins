package io.freefair.gradle.plugins.quicktype.internal

import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.tasks.DefaultSourceSet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.SourceSet

open class DefaultQuicktypeSourceSet(objectFactory: ObjectFactory, sourceSet: SourceSet) {
    val quicktype: SourceDirectorySet = objectFactory.sourceDirectorySet("quicktype", (sourceSet as DefaultSourceSet).displayName + " quicktype source")
    val allQuicktype: SourceDirectorySet = objectFactory.sourceDirectorySet("all${sourceSet.name}", (sourceSet as DefaultSourceSet).displayName + " quicktype source")

    var quicktypeConfigurationName: String = "quicktype"
    var quicktypePath: FileCollection? = null

    var inpathConfigurationName: String = "quicktypeIn"
    var inPath: FileCollection? = null

    init {
        quicktype.filter.include("**/*.d.ts", "**/*.ts")
        allQuicktype.source(quicktype)
        allQuicktype.filter.include("**/*.ts")
    }
}
