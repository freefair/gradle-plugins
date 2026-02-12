package io.freefair.gradle.plugins.quicktype

import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Baseline tests for QuicktypePlugin.
 */
class QuicktypePluginTest {

    private lateinit var project: org.gradle.api.Project

    @BeforeEach
    fun setUp() {
        project = ProjectBuilder.builder().build()
    }

    @Test
    fun pluginApplies() {
        project.plugins.apply(QuicktypePlugin::class.java)

        assertThat(project.plugins.hasPlugin(QuicktypePlugin::class.java)).isTrue()
        assertThat(project.plugins.hasPlugin(JavaPlugin::class.java)).isTrue()
    }
}
