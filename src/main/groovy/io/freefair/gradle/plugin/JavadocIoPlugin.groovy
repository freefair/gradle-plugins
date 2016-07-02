package io.freefair.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.javadoc.Javadoc

class JavadocIoPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        def config = project.configurations.create("javadocIo");

        Copy prepareJavadocTask = project.task ("prepareJavadoc", type: Copy) { Copy cp ->
            cp.destinationDir = project.file("$project.buildDir/exploded-javadoc");
            cp.group = JavaBasePlugin.DOCUMENTATION_GROUP
            cp.description = "Download and extract all javadocs, so the javadoc-tasks can link against them"
        } as Copy

        project.tasks.withType(Javadoc) { jdTask ->
            jdTask.dependsOn prepareJavadocTask;
            jdTask.inputs.file(prepareJavadocTask.destinationDir)
        }

        project.afterEvaluate {
            config.resolvedConfiguration.firstLevelModuleDependencies.each { resolvedDependency ->

                def string = "$resolvedDependency.moduleGroup/$resolvedDependency.moduleName/$resolvedDependency.moduleVersion"

                def file = resolvedDependency.moduleArtifacts.first().file
                prepareJavadocTask.into(string) {
                    from project.zipTree(file)
                }

                def dir = new File(prepareJavadocTask.destinationDir, string);
                project.tasks.withType(Javadoc) {
                    options.linksOffline (
                            "http://static.javadoc.io/$resolvedDependency.moduleGroup/$resolvedDependency.moduleName/$resolvedDependency.moduleVersion",
                            dir.absolutePath
                    )
                }
            }
        }
    }
}
