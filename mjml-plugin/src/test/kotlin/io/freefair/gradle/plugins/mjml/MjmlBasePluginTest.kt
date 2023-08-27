package io.freefair.gradle.plugins.mjml

import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class MjmlBasePluginTest {
    private lateinit var project: Project

    @TempDir
    private lateinit var gradleUserHome: File

    @BeforeEach
    fun setUp() {
        project = ProjectBuilder.builder()
            .withProjectDir(File("."))
            .withGradleUserHomeDir(gradleUserHome)
            .build()
    }

    @Test
    fun apply() {
        project.plugins.apply(MjmlBasePlugin::class.java)
        val extension = project.extensions.getByType<MjmlExtension>()
        assertThat(extension).isNotNull
    }
}
