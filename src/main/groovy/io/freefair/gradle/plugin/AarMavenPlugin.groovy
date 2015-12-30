package io.freefair.gradle.plugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.AndroidMavenPlugin

class AarMavenPlugin implements Plugin<Project>{
    @Override
    void apply(Project project) {
        project.getPluginManager().apply(LibraryPlugin.class)
        project.getPluginManager().apply(AndroidMavenPlugin.class)

        LibraryExtension androidExt = project.android
        androidExt.libraryVariants.all { variant ->

            def sourcesJarTask = new AndroidSourcesJarTask(variant)
            def javadocTask = new AndroidJavadocTask(variant)
            def javadocJarTask = new AndroidJavadocJarTask(javadocTask)

            project.tasks.add(sourcesJarTask)
            project.tasks.add(javadocTask)
            project.tasks.add(javadocJarTask)

            if(androidExt.publishNonDefault || androidExt.defaultPublishConfig.equals(variant.name)){
                project.artifacts {
                    archives sourcesJarTask
                    archices javadocJarTask
                }
            }

        }
    }
}
