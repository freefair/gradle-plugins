package io.freefair.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.tasks.Jar

class AndroidJavadocJarPlugin extends AndroidProjectPlugin {

    @Override
    void apply(Project project) {
        super.apply(project)

        Task allJavadocTask = project.task("javadoc") { Task ajdTasks ->
            ajdTasks.description = "Generate Javadoc for all variants"
            ajdTasks.group = JavaBasePlugin.DOCUMENTATION_GROUP
        }

        Task allJavadocJarTask = project.task("javadocJar") { Task ajdjTask ->
            ajdjTask.description = "Generate the javadoc jar for all variants"
            ajdjTask.group = "jar"
        }

        androidVariants.all { variant ->

            Javadoc javadocTask = project.task("javadoc${variant.name.capitalize()}", type: Javadoc) { Javadoc javadoc ->
                javadoc.description = "Generate Javadoc for the $variant.name variant"
                javadoc.group = JavaBasePlugin.DOCUMENTATION_GROUP;

                javadoc.source = variant.javaCompiler.source
                javadoc.classpath = variant.javaCompiler.classpath + project.files(androidExt.getBootClasspath())

                javadoc.exclude '**/BuildConfig.java'
                javadoc.exclude '**/R.java'

                if (javadoc.getOptions() instanceof StandardJavadocDocletOptions) {
                    StandardJavadocDocletOptions realOptions = javadoc.options as StandardJavadocDocletOptions

                    realOptions.links "http://docs.oracle.com/javase/7/docs/api/"
                    realOptions.linksOffline "http://developer.android.com/reference/", "${androidExt.sdkDirectory}/docs/reference"
                    realOptions.addStringOption('Xdoclint:none', '-quiet')
                }

                javadoc.setFailOnError false

                if (project.hasProperty("docsDir")) {
                    javadoc.destinationDir = new File(project.docsDir, "javadoc/${variant.dirName}")
                }

            } as Javadoc

            allJavadocTask.dependsOn javadocTask

            Jar javadocJarTask = project.task("javadoc${variant.name.capitalize()}Jar", type: Jar, dependsOn: javadocTask) { Jar jar ->
                jar.description = "Generate the javadoc jar for the ${variant.name} variant"
                jar.group = "jar"

                jar.appendix = variant.name
                jar.classifier = 'javadoc'
                jar.from javadocTask.destinationDir
            } as Jar

            allJavadocJarTask.dependsOn javadocJarTask

            if (publishVariant(variant)) {
                project.artifacts.add(Dependency.ARCHIVES_CONFIGURATION, javadocJarTask)
            }

        }
    }
}
