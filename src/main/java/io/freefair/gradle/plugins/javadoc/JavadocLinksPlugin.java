package io.freefair.gradle.plugins.javadoc;

import io.freefair.gradle.plugins.base.AbstractExtensionPlugin;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.Nullable;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.UnionFileCollection;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import java.util.concurrent.Callable;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

@Getter
public class JavadocLinksPlugin extends AbstractExtensionPlugin<JavadocLinksExtension> {

    private Task configureJavadocLinks;

    @Override
    public void apply(final Project project) {
        super.apply(project);

        project.getTasks().withType(Javadoc.class, new Action<Javadoc>() {
            @Override
            public void execute(final Javadoc javadoc) {
                configure(javadoc);
            }
        });

    }

    @Override
    protected JavadocLinksExtension createExtension() {
        return project.getExtensions().create(getExtensionName(), getExtensionClass(), getProject());
    }

    private void configure(final Javadoc javadoc) {
        String taskName = "configure" + capitalize((CharSequence) javadoc.getName()) + "Links";

        ConfigureJavadocLinks configureJavadocLinks = project.getTasks().create(taskName, ConfigureJavadocLinks.class);
        configureJavadocLinks.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
        configureJavadocLinks.setJavadoc(javadoc);
        javadoc.dependsOn(configureJavadocLinks);

        configureJavadocLinks.setConfiguration(project.provider(
                new Callable<Configuration>() {
                    @Override
                    public Configuration call() throws Exception {
                        return findConfiguraion(javadoc.getClasspath());
                    }
                }
        ));

        configureJavadocLinks.setJavaVersion(extension.getJavaVersionProvider());

        configureJavadocLinks.doFirst(new Action<Task>() {
            @Override
            public void execute(Task configureJavadocLinks) {
                project.getTasks().withType(Javadoc.class, new Action<Javadoc>() {
                    @Override
                    public void execute(Javadoc javadoc) {
                        StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) javadoc.getOptions();
                        for (String link : extension.getLinks()) {
                            project.getLogger().info("Adding link {} to javadoc task {}", link, javadoc);
                            options.links(link);
                        }
                    }
                });
            }
        });
    }

    @Override
    protected Class<JavadocLinksExtension> getExtensionClass() {
        return JavadocLinksExtension.class;
    }

    @Nullable
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
