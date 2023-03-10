package io.freefair.gradle.plugins.maven.javadoc;

import lombok.experimental.UtilityClass;
import org.gradle.api.JavaVersion;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

/**
 * @author Lars Grefer
 */
@UtilityClass
public class JavadocLinkUtil {

    public JavaVersion getJavaVersion(Javadoc javadoc) {
        if (javadoc.getJavadocTool().isPresent()) {
            JavaLanguageVersion languageVersion = javadoc.getJavadocTool().get().getMetadata().getLanguageVersion();
            return JavaVersion.toVersion(languageVersion.asInt());
        }

        JavaPluginExtension javaPluginExtension = javadoc.getProject().getExtensions().findByType(JavaPluginExtension.class);

        if (javaPluginExtension != null) {
            return javaPluginExtension.getSourceCompatibility();
        }

        return JavaVersion.current();
    }

    public static String getJavaSeLink(JavaVersion javaVersion) {

        if (javaVersion.isJava11Compatible()) {
            return "https://docs.oracle.com/en/java/javase/" + javaVersion.getMajorVersion() + "/docs/api/";
        }
        else {
            return "https://docs.oracle.com/javase/" + javaVersion.getMajorVersion() + "/docs/api/";
        }
    }
}
