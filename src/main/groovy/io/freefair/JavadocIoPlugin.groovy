package io.freefair

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.javadoc.Javadoc

class JavadocIoPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {


        def config = project.configurations.maybeCreate("javadocIo");

        Copy prepareJavadocTask = project.task ("prepareJavadoc", type: Copy) { Copy cp ->
            cp.destinationDir = project.file("$project.buildDir/exploded-javadoc");
        } as Copy

        project.tasks.withType(Javadoc) { jdTask ->
            jdTask.dependsOn prepareJavadocTask;
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
