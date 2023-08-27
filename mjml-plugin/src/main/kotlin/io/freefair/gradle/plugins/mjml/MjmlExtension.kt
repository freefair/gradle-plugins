package io.freefair.gradle.plugins.mjml

import org.gradle.api.provider.Property

abstract class MjmlExtension {
    abstract val minify: Property<Boolean>
    abstract val beautify: Property<Boolean>
    abstract val minifyOptions: Property<String>
    abstract val juiceOptions: Property<String>
    abstract val juicePreserveTags: Property<String>
    abstract val validationMode: Property<ValidationMode>
}

enum class ValidationMode {
    strict, normal, skip
}
