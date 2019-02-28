package io.freefair.gradle.plugins.maven;

import io.freefair.gradle.plugins.maven.javadoc.JavadocJarPlugin;
import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.plugins.signing.SigningExtension;
import org.gradle.plugins.signing.SigningPlugin;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

@Getter
public abstract class MavenPublishBasePlugin implements Plugin<Project> {

    private Project project;
    private MavenPublication publication;

    @Override
    public void apply(Project project) {
        this.project = project;
        project.getPlugins().apply(getPluginClass());
        project.getPlugins().apply(MavenPublishPlugin.class);

        publication = project.getExtensions().getByType(PublishingExtension.class)
                .getPublications()
                .create(getPublicationName(), MavenPublication.class);

        project.afterEvaluate(p -> {
            publication.from(getSoftwareComponent());

            project.getPlugins().withType(SourcesJarPlugin.class, sourcesJarPlugin -> {
                Jar sourcesJar = sourcesJarPlugin.getSourcesJar().get();
                String classifier = sourcesJar.getArchiveClassifier().getOrElse("sources");
                publication.artifact(sourcesJar, config -> config.setClassifier(classifier));
            });

            project.getPlugins().withType(JavadocJarPlugin.class, javadocJarPlugin -> {
                Jar javadocJar = javadocJarPlugin.getJavadocJar().get();
                String classifier = javadocJar.getArchiveClassifier().getOrElse("javadoc");
                publication.artifact(javadocJar, config -> config.setClassifier(classifier));
            });

            project.getPlugins().withType(SigningPlugin.class, signingPlugin -> project.getExtensions()
                    .getByType(SigningExtension.class)
                    .sign(publication)
            );
        });
    }

    protected abstract Class<? extends Plugin> getPluginClass();

    public String getPublicationName() {
        return "maven" + capitalize((CharSequence) getComponentName());
    }

    public SoftwareComponent getSoftwareComponent() {
        return getProject().getComponents().getByName(getComponentName());
    }

    abstract String getComponentName();
}
