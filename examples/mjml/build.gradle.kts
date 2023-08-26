import io.freefair.gradle.plugins.mjml.ValidationMode

plugins {
    kotlin("jvm")
    id("io.freefair.mjml.java")
}

mjml {
    validationMode = ValidationMode.strict
    minify = true
    beautify = false
    minifyOptions = "{\"minifyCSS\": true, \"removeEmptyAttributes\": false}"
    juiceOptions = "{\"preserveImportant\": true}"
    juicePreserveTags = "{\"myTag\": { \"start\": \"<#\", \"end\": \"</#\" }}"
}