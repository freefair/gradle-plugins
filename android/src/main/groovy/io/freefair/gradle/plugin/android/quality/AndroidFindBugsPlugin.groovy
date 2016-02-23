package io.freefair.gradle.plugin.android.quality

import com.android.build.gradle.api.AndroidSourceSet
import org.gradle.api.Incubating
import org.gradle.api.Task
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.plugins.quality.FindBugs
import org.gradle.api.plugins.quality.FindBugsExtension
import org.gradle.api.reporting.Report

/**
 * Copy of {@link org.gradle.api.plugins.quality.FindBugsPlugin} which
 * <ul>
 * <li>extends {@link AbstractAndroidCodeQualityPlugin} instead of {@link org.gradle.api.plugins.quality.internal.AbstractCodeQualityPlugin}</li>
 * <li>uses {@link AndroidSourceSet AndroidSourceSets} instead of {@link org.gradle.api.tasks.SourceSet JavaSourceSets}</li>
 * </ul>
 *
 * @see org.gradle.api.plugins.quality.FindBugsPlugin
 * @see AbstractAndroidCodeQualityPlugin
 */
@Incubating
class AndroidFindBugsPlugin extends AbstractAndroidCodeQualityPlugin<FindBugs> {
    public static final String DEFAULT_FINDBUGS_VERSION = "3.0.1"
    private FindBugsExtension extension

    @Override
    protected String getToolName() {
        return "FindBugs"
    }

    @Override
    protected Class<FindBugs> getTaskType() {
        return FindBugs
    }

    @Override
    protected void beforeApply() {
        configureFindBugsConfigurations()
    }

    private configureFindBugsConfigurations() {
        project.configurations.create('findbugsPlugins').with {
            visible = false
            transitive = true
            description = 'The FindBugs plugins to be used for this project.'
        }
    }

    @Override
    protected CodeQualityExtension createExtension() {
        extension = project.extensions.create("findbugs", FindBugsExtension, project)
        extension.toolVersion = DEFAULT_FINDBUGS_VERSION
        return extension
    }

    @Override
    protected void configureTaskDefaults(FindBugs task, String baseName) {
        task.with {
            pluginClasspath = project.configurations['findbugsPlugins']
        }
        def config = project.configurations['findbugs']
        config.defaultDependencies { dependencies ->
            dependencies.add(this.project.dependencies.create("com.google.code.findbugs:findbugs:${this.extension.toolVersion}"))
        }
        task.conventionMapping.with {
            findbugsClasspath = { config }
            ignoreFailures = { extension.ignoreFailures }
            effort = { extension.effort }
            reportLevel = { extension.reportLevel }
            visitors = { extension.visitors }
            omitVisitors = { extension.omitVisitors }

            excludeFilterConfig = { extension.excludeFilterConfig }
            includeFilterConfig = { extension.includeFilterConfig }
            excludeBugsFilterConfig = { extension.excludeBugsFilterConfig }

            extraArgs = { extension.extraArgs }
        }
        task.reports.all { Report report ->
            report.conventionMapping.with {
                enabled = { report.name == "xml" }
                destination = { new File(extension.reportsDir, "${baseName}.${report.name}") }
            }
        }
    }

    @Override
    protected void configureForSourceSet(AndroidSourceSet sourceSet, FindBugs task) {
        task.with {
            description = "Run FindBugs analysis for ${sourceSet.name} classes"
        }
        task.source = sourceSet.java.sourceFiles
        task.conventionMapping.with {
            classes = {
                // the simple "classes = sourceSet.output" may lead to non-existing resources directory
                // being passed to FindBugs Ant task, resulting in an error
                project.fileTree(sourceSet.output.classesDir) {
                    builtBy sourceSet.output
                }
            }
            classpath = { sourceSet.compileClasspath }
        }
    }

    @Override
    protected void configureBaseTask(Task task) {
        task.description = "Run FindBugs analysis for all classes"
    }
}
