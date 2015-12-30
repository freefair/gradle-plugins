package io.freefair.gradle.plugin
import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.internal.DefaultDomainObjectSet

class AndroidMavenPlugin implements Plugin<Project>{
    @Override
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
        } catch (Exception e){
            project.logger.debug("No Application found", e)
        }

        if (variants == null ){
            project.logger.error("No Android Variants found")
            return;
        }

        variants.all { variant ->

            def sourcesJarTask = new AndroidSourcesJarTask(variant)
            def javadocTask = new AndroidJavadocTask(variant)
            def javadocJarTask = new AndroidJavadocJarTask(javadocTask)

            project.tasks.add(sourcesJarTask)
            project.tasks.add(javadocTask)
            project.tasks.add(javadocJarTask)

            if(libraryExtension == null && (libraryExtension.publishNonDefault || libraryExtension.defaultPublishConfig.equals(variant.name))){
                project.artifacts.add(Dependency.ARCHIVES_CONFIGURATION, sourcesJarTask)
                project.artifacts.add(Dependency.ARCHIVES_CONFIGURATION, javadocJarTask)
            }

        }
    }
}
