package io.freefair.gradle.plugin.android.quality

import com.android.build.gradle.api.AndroidSourceSet
import org.gradle.api.Task
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.CodeQualityExtension

/**
 * Copy of {@link org.gradle.api.plugins.quality.CheckstylePlugin} which
 * <ul>
 * <li>extends {@link AbstractAndroidCodeQualityPlugin} instead of {@link org.gradle.api.plugins.quality.internal.AbstractCodeQualityPlugin}</li>
 * <li>uses {@link AndroidSourceSet AndroidSourceSets} instead of {@link org.gradle.api.tasks.SourceSet JavaSourceSets}</li>
 * </ul>
 *
 * @see org.gradle.api.plugins.quality.CheckstylePlugin
 * @see AbstractAndroidCodeQualityPlugin
 */
class AndroidCheckstylePlugin extends AbstractAndroidCodeQualityPlugin<Checkstyle> {

    public static final String DEFAULT_CHECKSTYLE_VERSION = "5.9"
    private CheckstyleExtension extension

    @Override
    protected String getToolName() {
        return "Checkstyle"
    }

    @Override
    protected Class<Checkstyle> getTaskType() {
        return Checkstyle
    }

    @Override
    protected CodeQualityExtension createExtension() {
        extension = project.extensions.create("checkstyle", CheckstyleExtension, project)

        extension.with {
            toolVersion = DEFAULT_CHECKSTYLE_VERSION
            config = project.resources.text.fromFile("config/checkstyle/checkstyle.xml")
        }

        return extension
    }

    @Override
    protected void configureTaskDefaults(Checkstyle task, String baseName) {
        def conf = project.configurations['checkstyle']
        conf.defaultDependencies { dependencies ->
            dependencies.add(this.project.dependencies.create("com.puppycrawl.tools:checkstyle:${this.extension.toolVersion}"))
        }

        task.conventionMapping.with {
            checkstyleClasspath = { conf }
            config = { extension.config }
            configProperties = { extension.configProperties }
            ignoreFailures = { extension.ignoreFailures }
            showViolations = { extension.showViolations }
        }

        task.reports.all { report ->
            report.conventionMapping.with {
                enabled = { true }
                destination = { new File(extension.reportsDir, "${baseName}.${report.name}") }
            }
        }
    }

    @Override
    protected void configureForSourceSet(AndroidSourceSet sourceSet, Checkstyle task) {
        task.with {
            description = "Run Checkstyle analysis for ${sourceSet.name} classes"
            classpath = sourceSet.java.sourceFiles
        }
        task.setSource(sourceSet.java.sourceFiles)
    }

    @Override
    protected void configureBaseTask(Task task) {
        task.description = "Run Checkstyle analysis for all classes"
    }
}
