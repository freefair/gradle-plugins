package io.freefair.gradle.plugins.maven.javadoc;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

/**
 * @author Lars Grefer
 */
public class JavadocUtf8Plugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getTasks().withType(Javadoc.class, javadoc -> {

            StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) javadoc.getOptions();

            options.charSet("UTF-8");
            options.docEncoding("UTF-8");
            options.setEncoding("UTF-8");
        });
    }
}
