package io.freefair.gradle.plugins.javadoc;

import io.freefair.gradle.plugins.base.AbstractPlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

/**
 * @author Lars Grefer
 */
public class JavadocUtf8Plugin extends AbstractPlugin {

    @Override
    public void apply(Project project) {
        super.apply(project);

        project.getTasks().withType(Javadoc.class, new Action<Javadoc>() {
            @Override
            public void execute(Javadoc javadoc) {

                StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) javadoc.getOptions();

                options.charSet("UTF-8");
                options.docEncoding("UTF-8");
            }
        });
    }
}
