package io.freefair.gradle.plugin

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions

/**
 * Created by larsgrefer on 30.12.15.
 */
class AndroidJavadocTask extends Javadoc {

    BaseVariant variant;

    public AndroidJavadocTask(BaseVariant variant){
        this.variant = variant;

        name = "javadoc${variant.name.capitalize()}"
        description = "Generate Javadoc for the $variant.name variant"

        source = variant.javaCompiler.source
        classpath = variant.javaCompiler.classpath

        if(getOptions() instanceof StandardJavadocDocletOptions){
            StandardJavadocDocletOptions realOptions = getOptions()

            realOptions.links "http://docs.oracle.com/javase/7/docs/api/"
            realOptions.links "http://developer.android.com/reference/"
        }

        setFailOnError(false)


    }
}
