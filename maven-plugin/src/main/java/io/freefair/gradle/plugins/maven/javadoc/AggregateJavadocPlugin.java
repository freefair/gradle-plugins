package io.freefair.gradle.plugins.maven.javadoc;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.attributes.VerificationType;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.jvm.internal.JvmPluginServices;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;

import javax.inject.Inject;

@Getter
public abstract class AggregateJavadocPlugin implements Plugin<Project> {

    private TaskProvider<Javadoc> javadocTask;
    private Configuration javadocConfiguration;
    private Configuration javadocSources;
    private Configuration javadocClasspath;
    private TaskProvider<Jar> javadocJar;

    @Inject
    public abstract JvmPluginServices getJvmPluginServices();

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaBasePlugin.class);

        project.getPlugins().withType(JavaPlugin.class, jp -> {
            throw new IllegalStateException("The aggregate-javadoc plugin should not be used on a java project");
        });

        createConfigurations(project);

        javadocTask = project.getTasks().register("javadoc", Javadoc.class);

        javadocTask.configure(jd -> {
            jd.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
            jd.source(javadocSources);
            jd.include("**/*.java");
            jd.setClasspath(javadocClasspath);

            jd.setFailOnError(false);
        });

        javadocJar = project.getTasks().register("javadocJar", Jar.class, jar -> {
            jar.setGroup(BasePlugin.BUILD_GROUP);
            jar.from(javadocTask);
            jar.getArchiveClassifier().convention("javadoc");
        });

        project.getArtifacts().add(javadocConfiguration.getName(), javadocJar);

        javadocConfiguration.getOutgoing().getVariants().create("javadocDirectory", dirVariant -> {
            dirVariant.artifact(javadocTask, cpa -> cpa.setType("directory"));
        });
    }

    private void createConfigurations(Project project) {
        javadocConfiguration = project.getConfigurations().create("javadoc");
        javadocConfiguration.setCanBeConsumed(true);
        javadocConfiguration.setCanBeResolved(false);
        javadocConfiguration.getAttributes().attribute(Category.CATEGORY_ATTRIBUTE, project.getObjects().named(Category.class, Category.DOCUMENTATION));
        javadocConfiguration.getAttributes().attribute(Bundling.BUNDLING_ATTRIBUTE, project.getObjects().named(Bundling.class, Bundling.EMBEDDED));
        javadocConfiguration.getAttributes().attribute(DocsType.DOCS_TYPE_ATTRIBUTE, project.getObjects().named(DocsType.class, DocsType.JAVADOC));

        javadocSources = project.getConfigurations().create("javadocSources");
        javadocSources.setTransitive(false);
        javadocSources.setCanBeConsumed(false);
        javadocSources.setCanBeResolved(true);
        javadocSources.getAttributes().attribute(DocsType.DOCS_TYPE_ATTRIBUTE, project.getObjects().named(DocsType.class, DocsType.SOURCES));
        javadocSources.getAttributes().attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, project.getObjects().named(VerificationType.class, VerificationType.MAIN_SOURCES));
        javadocSources.extendsFrom(javadocConfiguration);

        javadocClasspath = project.getConfigurations().create("javadocClasspath");
        javadocClasspath.setCanBeConsumed(false);
        javadocClasspath.setCanBeResolved(true);
        getJvmPluginServices().configureAsRuntimeClasspath(javadocClasspath);
        javadocClasspath.extendsFrom(javadocConfiguration);
    }
}
