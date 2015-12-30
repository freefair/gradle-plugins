package io.freefair.gradle.plugin
import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.tasks.Jar

class AndroidMavenPlugin implements Plugin<Project> {

    void apply(Project project) {

        DefaultDomainObjectSet<BaseVariant> variants = null;
        LibraryExtension libraryExtension = null;

        try {
            libraryExtension = project.android
            variants = libraryExtension.libraryVariants;
        } catch (Exception e) {
            project.logger.debug("No Library found", e)
        }

        try {
            AppExtension appExt = project.android
            variants = appExt.applicationVariants;
        } catch (Exception e) {
            project.logger.debug("No Application found", e)
        }

        if (variants == null) {
            project.logger.error("No Android Variants found")
            return;
        }

        Task allSourcesJarTask = project.task("sourcesJar") { Task j ->
            j.description = "Generate the sources jar for all variants"
            j.group = "jar"
        }

        Task allJavadocTask = project.task("javadoc") { Task jd ->
            jd.description = "Generate Javadoc for all variants"
            jd.group = JavaBasePlugin.DOCUMENTATION_GROUP
        }

        Task allJavadocJarTask = project.task("sourcesJar") { Task j ->
            j.description = "Generate the javadoc jar for all variants"
            j.group = "jar"
        }

        variants.all { variant ->

            Jar sourcesJarTask = project.task("sources${variant.name.capitalize()}Jar", type: Jar) { Jar jar ->
                jar.description = "Generate the sources jar for the $variant.name variant"
                jar.group = "jar"

                jar.classifier = "sources"
                jar.appendix = variant.name;
                jar.from variant.javaCompiler.source
            } as Jar

            allSourcesJarTask.dependsOn sourcesJarTask

            Javadoc javadocTask = project.task("javadoc${variant.name.capitalize()}", type: Javadoc) { Javadoc javadoc ->
                javadoc.description = "Generate Javadoc for the $variant.name variant"
                javadoc.group = JavaBasePlugin.DOCUMENTATION_GROUP;

                javadoc.source = variant.javaCompiler.source
                javadoc.classpath = variant.javaCompiler.classpath

                if (javadoc.getOptions() instanceof StandardJavadocDocletOptions) {
                    StandardJavadocDocletOptions realOptions = getOptions()

                    realOptions.links "http://docs.oracle.com/javase/7/docs/api/"
                    realOptions.links "http://developer.android.com/reference/"
                }

                javadoc.setFailOnError false

                if (project.hasProperty("docsDir")) {
                    File baseDocsDir = project.docsDir;
                    javadoc.destinationDir = new File(baseDocsDir, "javadoc/${variant.dirName}")
                }

            } as Javadoc

            allJavadocTask.dependsOn javadocTask

            Jar javadocJarTask = project.task("javadoc${variant.name.capitalize()}Jar", type: Jar, dependsOn: javadocTask) { Jar jar ->
                jar.description = "Generate the javadoc jar for the ${variant.name} variant"

                jar.appendix = variant.name
                jar.classifier = 'javadoc'
                jar.from javadocTask.destinationDir
            } as Jar

            allJavadocJarTask.dependsOn javadocJarTask

            if (libraryExtension == null || (libraryExtension.publishNonDefault || libraryExtension.defaultPublishConfig.equals(variant.name))) {
                project.artifacts.add(Dependency.ARCHIVES_CONFIGURATION, sourcesJarTask)
                project.artifacts.add(Dependency.ARCHIVES_CONFIGURATION, javadocJarTask)
            }

        }
    }
}
