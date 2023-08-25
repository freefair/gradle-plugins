plugins {
    `kotlin-dsl`
    id("maven-publish")
    id("com.gradle.plugin-publish")
}

description = "MJML processor Gradle plugins"

dependencies {
    implementation("org.apache.maven:maven-model:3.9.4")
    implementation("com.github.node-gradle:gradle-node-plugin:7.0.0")

    testImplementation(project(":test-common"))
}

gradlePlugin {
    plugins {
        this.create("mjmlBase") {
            id = "io.freefair.mjml.base"
            implementationClass = "io.freefair.gradle.plugins.mjml.MjmlBasePlugin"
            displayName = "MJML Plugin"
            description = "MJML Plugin"
            tags.set(listOf("mjml"))
        }
        this.create("mjmlKotlin") {
            id = "io.freefair.mjml.java"
            implementationClass = "io.freefair.gradle.plugins.mjml.MjmlJavaPlugin"
            displayName = "MJML Java Plugin"
            description = "MJML Java Plugin"
            tags.set(listOf("mjml"))
        }
    }
}