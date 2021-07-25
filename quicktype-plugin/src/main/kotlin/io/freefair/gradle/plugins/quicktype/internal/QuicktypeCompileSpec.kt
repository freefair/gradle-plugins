package io.freefair.gradle.plugins.quicktype.internal

import org.gradle.api.file.FileCollection
import org.gradle.language.base.internal.compile.CompileSpec

open class QuicktypeCompileSpec : CompileSpec {
    var quicktypeCompileOptions: QuicktypeCompileOptions? = null

    private val additionalInpath: FileCollection? = null
}
