package io.freefair.gradle.plugin.android.maven

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidMavenJarsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply(AndroidSourcesJarPlugin.class)
        project.pluginManager.apply(AndroidJavadocJarPlugin.class)
    }
}
