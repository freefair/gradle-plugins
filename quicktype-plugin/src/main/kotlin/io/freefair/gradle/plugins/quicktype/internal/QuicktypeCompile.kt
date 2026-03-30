package io.freefair.gradle.plugins.quicktype.internal

import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.api.tasks.compile.CompileOptions

@CacheableTask
abstract class QuicktypeCompile : AbstractCompile() {
    @Nested
    val options = project.objects.newInstance(CompileOptions::class.java)


}
