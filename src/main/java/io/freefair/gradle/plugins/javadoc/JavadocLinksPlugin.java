package io.freefair.gradle.plugins.javadoc;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.UnionFileCollection;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

@Getter
public class JavadocLinksPlugin implements Plugin<Project> {

    private JavadocLinksExtension javadocLinks;

    @Override
    public void apply(Project project) {
        javadocLinks = project.getExtensions().create("javadocLinks", JavadocLinksExtension.class);

        project.getTasks().withType(Javadoc.class, javadoc -> {
            String taskName = "configure" + capitalize((CharSequence) javadoc.getName()) + "Links";

            ConfigureJavadocLinks configureJavadocLinks = project.getTasks().create(taskName, ConfigureJavadocLinks.class);
            configureJavadocLinks.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
            configureJavadocLinks.setJavadoc(javadoc);
            javadoc.dependsOn(configureJavadocLinks);

            project.afterEvaluate(p -> {
                configureJavadocLinks.setJavaVersion(javadocLinks.getJavaVersion());
                configureJavadocLinks.setConfiguration(findConfiguraion(javadoc.getClasspath()));
            });

            configureJavadocLinks.doFirst(configureJavadocLinks1 ->
                    project.getTasks().withType(Javadoc.class, javadoc1 -> {
                        StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) javadoc1.getOptions();
                        for (String link : javadocLinks.getLinks()) {
                            project.getLogger().info("Adding link {} to javadoc task {}", link, javadoc1);
                            options.links(link);
                        }
                    }));
        });
    }

    private Configuration findConfiguraion(FileCollection classpath) {
        if (classpath instanceof Configuration) {
            return (Configuration) classpath;
        } else if (classpath instanceof UnionFileCollection) {
            for (FileCollection files : ((UnionFileCollection) classpath).getSources()) {
                Configuration configuraion = findConfiguraion(files);
                if (configuraion != null) return configuraion;
            }
        }

        return null;
    }
}
