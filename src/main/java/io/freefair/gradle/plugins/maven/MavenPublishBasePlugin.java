package io.freefair.gradle.plugins.maven;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

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

            project.getPlugins().withType(SourcesJarPlugin.class, sourcesJarPlugin ->
                    publication.artifact(sourcesJarPlugin.getJarTask(), a -> a.setClassifier(sourcesJarPlugin.getClassifier()))
            );

            project.getPlugins().withType(JavadocJarPlugin.class, javadocJarPlugin ->
                    publication.artifact(javadocJarPlugin.getJarTask(), a -> a.setClassifier(javadocJarPlugin.getClassifier()))
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
