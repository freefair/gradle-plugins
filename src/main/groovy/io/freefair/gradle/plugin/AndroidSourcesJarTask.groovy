package io.freefair.gradle.plugin
import com.android.build.gradle.api.BaseVariant
import org.gradle.jvm.tasks.Jar

class AndroidSourcesJarTask extends Jar {

    AndroidSourcesJarTask(BaseVariant libraryVariant){
        name = "sources${libraryVariant.name.capitalize()}Jar"
        description = "Generate the source jar for the $libraryVariant.name variant"
        this.classifier = "sources"
        from libraryVariant.javaCompiler.source
    }
}
