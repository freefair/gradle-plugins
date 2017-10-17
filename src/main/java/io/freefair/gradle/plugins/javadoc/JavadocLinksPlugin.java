package io.freefair.gradle.plugins.javadoc;

import lombok.Getter;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.UnionFileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.MinimalJavadocOptions;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import java.util.List;

@Getter
public class JavadocLinksPlugin implements Plugin<Project> {

    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;

        project.getTasks().withType(Javadoc.class, javadoc -> javadoc.doFirst(task -> {

            addLink(javadoc, getJavaSeLink(JavaVersion.current()));

            Configuration configuration = findConfiguraion(javadoc.getClasspath());

            boolean usedJavadocIo = false;

            for (ResolvedArtifact resolvedArtifact : configuration.getResolvedConfiguration().getResolvedArtifacts()) {
                ComponentIdentifier componentIdentifier = resolvedArtifact.getId().getComponentIdentifier();

                if (componentIdentifier instanceof ModuleComponentIdentifier) {
                    ModuleComponentIdentifier moduleComponentIdentifier = (ModuleComponentIdentifier) componentIdentifier;
                    String group = moduleComponentIdentifier.getGroup();
                    String artifact = moduleComponentIdentifier.getModule();
                    String version = moduleComponentIdentifier.getVersion();

                    String wellKnownLink = findWellKnownLink(group, artifact, version);
                    if (wellKnownLink != null) {
                        javadoc.getLogger().info("Using well known link '{}' for '{}:{}:{}'", wellKnownLink, group, artifact, version);
                        addLink(javadoc, wellKnownLink);
                        break;
                    } else {
                        javadoc.getLogger().info("Using javadoc.io link for '{}:{}:{}'", group, artifact, version);
                        String javadocIoLink = String.format("https://static.javadoc.io/%s/%s/%s/", group, artifact, version);
                        addLink(javadoc, javadocIoLink);
                        usedJavadocIo = true;
                    }
                }
            }

            if (usedJavadocIo && javadoc.getOptions().getJFlags().stream().noneMatch(flag -> flag.contains("http.agent"))) {
                javadoc.getOptions().jFlags("-Dhttp.agent=" + System.currentTimeMillis());
            }
        }));
    }

    private void addLink(Javadoc javadoc, String link) {
        MinimalJavadocOptions options = javadoc.getOptions();
        if (options instanceof StandardJavadocDocletOptions) {
            StandardJavadocDocletOptions docletOptions = (StandardJavadocDocletOptions) options;
            Logger logger = javadoc.getLogger();
            List<String> links = docletOptions.getLinks();

            if (!links.contains(link)) {
                logger.debug("Adding '{}' to {}", link, javadoc);
                links.add(link);
            } else {
                logger.info("Not adding '{}' to {} because it's already present", link, javadoc);
            }
        }
    }

    private String findWellKnownLink(String group, String artifact, String version) {
        if (group.equals("org.springframework") && artifact.startsWith("spring-")) {
            return "https://docs.spring.io/spring/docs/" + version + "/javadoc-api/";
        } else if (group.equals("org.springframework.boot")) {
            return "https://docs.spring.io/spring-boot/docs/" + version + "/api/";
        }
        return null;
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

    private String getJavaSeLink(JavaVersion javaVersion) {
        switch (javaVersion) {
            case VERSION_1_5:
                return "https://docs.oracle.com/javase/5/docs/api/";
            case VERSION_1_6:
                return "https://docs.oracle.com/javase/6/docs/api/";
            case VERSION_1_7:
                return "https://docs.oracle.com/javase/7/docs/api/";
            case VERSION_1_8:
                return "https://docs.oracle.com/javase/8/docs/api/";
            case VERSION_1_9:
                return "https://docs.oracle.com/javase/9/docs/api/";
            default:
                project.getLogger().warn("Unknown java version {}", javaVersion);
                return null;
        }
    }
}
