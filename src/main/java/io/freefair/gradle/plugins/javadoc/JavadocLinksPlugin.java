package io.freefair.gradle.plugins.javadoc;

import io.freefair.gradle.plugins.base.AbstractExtensionPlugin;
import lombok.Getter;
import org.gradle.api.*;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

@Getter
public class JavadocLinksPlugin extends AbstractExtensionPlugin<JavadocLinksExtension> {

    private Task configureJavadocLinks;

    @Override
    public void apply(final Project project) {
        super.apply(project);

        setupBase();

        setupDependencyGuesses();
    }

    @Override
    protected void afterEvaluate(Project project) {
        super.afterEvaluate(project);

        setupDocsOracleLink();
    }

    @Override
    protected Class<JavadocLinksExtension> getExtensionClass() {
        return JavadocLinksExtension.class;
    }

    private void setupDependencyGuesses() {

        project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);

        Task guessJavadocLinks = project.getTasks().create("guessJavadocLinks");
        guessJavadocLinks.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);

        getConfigureJavadocLinks().dependsOn(guessJavadocLinks);

        guessJavadocLinks.doFirst(new Action<Task>() {
            @Override
            public void execute(Task guessJavadocLinks) {
                for (ResolvedArtifact resolvedArtifact : project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME).getResolvedConfiguration().getResolvedArtifacts()) {

                    String name = resolvedArtifact.getName();
                    String version = resolvedArtifact.getModuleVersion().getId().getVersion();

                    String majorVersion;
                    if (version.contains(".")) {
                        majorVersion = version.substring(0, version.indexOf("."));
                    } else {
                        majorVersion = version;
                    }

                    switch (name) {
                        case "spring-boot":
                            extension.links("http://docs.spring.io/spring-boot/docs/" + version + "/api/");
                            break;
                        case "spring-core":
                        case "spring-context":
                        case "spring-beans":
                        case "spring-web":
                            extension.links("http://docs.spring.io/spring/docs/" + version + "/javadoc-api/");
                            break;
                        case "okio":
                        case "okhttp":
                        case "retrofit":
                            extension.links("http://square.github.io/" + name + "/" + majorVersion + ".x/" + name + "/");
                            break;
                    }
                }
            }
        });

    }

    private void setupBase() {

        configureJavadocLinks = project.getTasks().create("configureJavadocLinks");
        configureJavadocLinks.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);

        project.getTasks().withType(Javadoc.class, new Action<Javadoc>() {
            @Override
            public void execute(Javadoc javadoc) {
                javadoc.dependsOn(configureJavadocLinks);
            }
        });

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

    private void setupDocsOracleLink() {

        JavaVersion javaVersion = extension.getJavaVersion();
        if (javaVersion == null) {
            project.getLogger().debug("Not adding Oracle JavaSE Docs link because javaVersion is null");
        } else {
            switch (javaVersion) {
                case VERSION_1_5:
                    extension.links("http://docs.oracle.com/javase/5/docs/api/");
                    break;
                case VERSION_1_6:
                    extension.links("http://docs.oracle.com/javase/6/docs/api/");
                    break;
                case VERSION_1_7:
                    extension.links("http://docs.oracle.com/javase/7/docs/api/");
                    break;
                default:
                    project.getLogger().warn("Unknown Java version {}", javaVersion);
                case VERSION_1_8:
                    extension.links("http://docs.oracle.com/javase/8/docs/api/");
                    break;
            }
        }

        Integer javaEEVersion = extension.getJavaEEVersion();
        if(javaEEVersion == null) {
            project.getLogger().debug("Not adding Oracle JavaEE Docs link because javaEEVersion is null");
        } else {
            switch (javaEEVersion) {
                case 6:
                case 7:
                    extension.links("https://docs.oracle.com/javaee/"+javaEEVersion+"/api/");
                default:
                    project.getLogger().warn("Unkown JavaEE version {}", javaEEVersion);
                    extension.links("https://docs.oracle.com/javaee/7/api/");
            }
        }
    }
}
