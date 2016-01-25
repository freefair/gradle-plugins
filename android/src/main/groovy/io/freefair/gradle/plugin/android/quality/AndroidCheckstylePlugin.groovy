package io.freefair.gradle.plugin.android.quality

import io.freefair.gradle.plugin.android.AndroidProjectPlugin
import org.gradle.api.Incubating
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.CheckstylePlugin
import org.gradle.api.reporting.ReportingExtension

@Incubating
class AndroidCheckstylePlugin extends AndroidProjectPlugin {

    @Incubating
    public static final String CONFIGURATION_NAME = "checkstyle"

    private CheckstyleExtension extension;

    private Project project

    @Override
    void apply(Project project) {
        super.apply(project)

        this.project = project;

        createConfigurations()
        createExtension()

        Task allCheckstyleTask = project.task("checkstyle") { Task acTask ->
            acTask.description = "Run checkstyle on all variants"
            acTask.group = JavaBasePlugin.VERIFICATION_GROUP
        }

        project.tasks.findByPath("check").dependsOn allCheckstyleTask

        androidVariants.all { variant ->

            Checkstyle checkstyleTask = project.task("checkstyle${variant.name.capitalize()}", type: Checkstyle) { Checkstyle cs ->
                cs.description = "Run Checkstyle analysis for ${variant.name} classes"
                cs.group = JavaBasePlugin.VERIFICATION_GROUP

                cs.source variant.javaCompiler.source - project.fileTree("${project.buildDir}/generated")
                cs.classpath = variant.javaCompiler.outputs.files
            } as Checkstyle

            configureTaskDefaults(checkstyleTask, variant.baseName)

            allCheckstyleTask.dependsOn checkstyleTask

        }
    }

    protected void createConfigurations() {
        project.configurations.create(CONFIGURATION_NAME).with {
            visible = false
            transitive = true
            description = "The checkstyle libraries to be used for this project."
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

    private void createExtension() {
        extension = project.extensions.create("checkstyle", CheckstyleExtension.class, project)

        extension.with {
            config = project.resources.text.fromFile("config/checkstyle/checkstyle.xml")
            toolVersion = CheckstylePlugin.DEFAULT_CHECKSTYLE_VERSION
        }

        extension.conventionMapping.with {
            reportsDir = { project.extensions.getByType(ReportingExtension).file("checkstyle") }
        }
    }

    protected void configureTaskDefaults(Checkstyle task, String baseName) {
        def conf = project.configurations[CONFIGURATION_NAME]
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
}
