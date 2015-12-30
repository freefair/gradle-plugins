package io.freefair.gradle.plugin

import org.gradle.jvm.tasks.Jar

/**
 * Created by larsgrefer on 30.12.15.
 */
class AndroidJavadocJarTask extends Jar {

    AndroidJavadocJarTask(AndroidJavadocTask androidJavadocTask){
        name = "javadoc${androidJavadocTask.variant.name.capitalize()}Jar"
        description = "Generate the javadoc jar for the ${androidJavadocTask.variant.name} variant"

        dependsOn androidJavadocTask
        this.from(androidJavadocTask.destinationDir)
    }
}
