package io.freefair.gradle.plugin.android.maven

import io.freefair.gradle.plugin.android.AndroidProjectPlugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency
import org.gradle.jvm.tasks.Jar

class AndroidSourcesJarPlugin extends AndroidProjectPlugin {

    @Override
    void apply(Project project) {
        super.apply(project)

        Task allSourcesJarTask = project.task("sourcesJar") { Task asjTask ->
            asjTask.description = "Generate the sources jar for all variants"
            asjTask.group = "jar"
        }

        androidVariants.all { variant ->

            Jar sourcesJarTask = project.task("sources${variant.name.capitalize()}Jar", type: Jar) { Jar jar ->
                jar.description = "Generate the sources jar for the $variant.name variant"
                jar.group = "jar"

                jar.classifier = "sources"
                jar.appendix = variant.name;
                jar.from variant.javaCompiler.source
            } as Jar

            allSourcesJarTask.dependsOn sourcesJarTask

            if (publishVariant(variant)) {
                project.artifacts.add(Dependency.ARCHIVES_CONFIGURATION, sourcesJarTask)
            }
        }
    }
}
