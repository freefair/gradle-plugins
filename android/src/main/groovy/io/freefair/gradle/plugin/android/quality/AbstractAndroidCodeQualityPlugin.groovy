package io.freefair.gradle.plugin.android.quality

import com.android.build.gradle.BasePlugin
import com.android.build.gradle.api.AndroidSourceSet
import io.freefair.gradle.plugin.android.AndroidProjectPlugin
import org.gradle.api.Project
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.reporting.ReportingExtension

/**
 * Copy of {@linke org.gradle.api.plugins.quality.internal.AbstractCodeQualityPlugin} which
 * uses {@link AndroidSourceSet AndroidSourceSets} instead of {@link org.gradle.api.tasks.SourceSet JavaSourceSets}
 *
 * @see org.gradle.api.plugins.quality.internal.AbstractCodeQualityPlugin
 */
abstract class AbstractAndroidCodeQualityPlugin<T> extends AndroidProjectPlugin {
    protected Project project
    protected CodeQualityExtension extension

    final void apply(Project project) {
        super.apply(project)
        this.project = project

        beforeApply()
        project.pluginManager.apply(ReportingBasePlugin)
        createConfigurations()
        extension = createExtension()
        configureExtensionRule()
        configureTaskRule()
        configureSourceSetRule()
        configureCheckTask()
    }

    protected abstract String getToolName()

    protected abstract Class<T> getTaskType()

    protected String getTaskBaseName() {
        return toolName.toLowerCase()
    }

    protected String getConfigurationName() {
        return toolName.toLowerCase()
    }

    protected String getReportName() {
        return toolName.toLowerCase()
    }

    protected Class<?> getBasePlugin() {
        return BasePlugin
    }

    protected void beforeApply() {
    }

    protected void createConfigurations() {
        project.configurations.create(configurationName).with {
            visible = false
            transitive = true
            description = "The ${toolName} libraries to be used for this project."
            // Don't need these things, they're provided by the runtime
            exclude group: 'ant', module: 'ant'
            exclude group: 'org.apache.ant', module: 'ant'
            exclude group: 'org.apache.ant', module: 'ant-launcher'
            exclude group: 'org.slf4j', module: 'slf4j-api'
            exclude group: 'org.slf4j', module: 'jcl-over-slf4j'
            exclude group: 'org.slf4j', module: 'log4j-over-slf4j'
            exclude group: 'commons-logging', module: 'commons-logging'
            exclude group: 'log4j', module: 'log4j'
        }
    }

    protected abstract CodeQualityExtension createExtension()

    private void configureExtensionRule() {
        extension.conventionMapping.with {
            sourceSets = { [] }
            reportsDir = { project.extensions.getByType(ReportingExtension).file(reportName) }
        }

        project.plugins.withType(basePlugin) {
            extension.conventionMapping.sourceSets = { androidExtension.sourceSets }
        }
    }

    private void configureTaskRule() {
        project.tasks.withType(taskType) { T task ->
            def prunedName = (task.name - taskBaseName ?: task.name)
            prunedName = prunedName[0].toLowerCase() + prunedName.substring(1)
            configureTaskDefaults(task, prunedName)
        }
    }

    protected void configureTaskDefaults(T task, String baseName) {
    }

    private void configureSourceSetRule() {
        project.plugins.withType(basePlugin) {
            androidExtension.sourceSets.all { AndroidSourceSet sourceSet ->
                T task = project.tasks.create("${taskBaseName}${sourceSet.name}", taskType)
                configureForSourceSet(sourceSet, task)
            }
        }
    }

    protected void configureForSourceSet(AndroidSourceSet sourceSet, T task) {
    }

    private void configureCheckTask() {
        project.plugins.withType(basePlugin) {
            project.tasks['check'].dependsOn { extension.sourceSets.collect { it.getTaskName(taskBaseName, null) } }
        }
    }
}
