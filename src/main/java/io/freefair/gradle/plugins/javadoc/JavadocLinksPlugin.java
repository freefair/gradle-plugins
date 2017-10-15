package io.freefair.gradle.plugins.javadoc;

import io.freefair.gradle.plugins.base.AbstractExtensionPlugin;
import lombok.Getter;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.UnionFileCollection;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import javax.annotation.Nullable;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

@Getter
public class JavadocLinksPlugin extends AbstractExtensionPlugin<JavadocLinksExtension> {

    private Task configureJavadocLinks;

    @Override
    public void apply(final Project project) {
        super.apply(project);

        project.getTasks().withType(Javadoc.class, javadoc -> configure(javadoc));

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
                () -> findConfiguraion(javadoc.getClasspath())
        ));

        configureJavadocLinks.setJavaVersion(extension.getJavaVersionProvider());

        configureJavadocLinks.doFirst(configureJavadocLinks1 ->
                project.getTasks().withType(Javadoc.class, javadoc1 -> {
                    StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) javadoc1.getOptions();
                    for (String link : extension.getLinks()) {
                        project.getLogger().info("Adding link {} to javadoc task {}", link, javadoc1);
                        options.links(link);
                    }
                }));
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
