package io.freefair.gradle.plugins.maven.javadoc;

import lombok.Getter;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.UnionFileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.MinimalJavadocOptions;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Getter
public class JavadocLinksPlugin implements Plugin<Project> {

    private Project project;

    private OkHttpClient okHttpClient;

    @Override
    public void apply(Project project) {
        this.project = project;

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(
                message -> project.getLogger().info(message)
        );
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        project.getTasks().withType(Javadoc.class, javadoc -> {

            addLink(javadoc, getJavaSeLink(JavaVersion.current()));

            javadoc.doFirst(task -> {
                findConfigurations(javadoc.getClasspath())
                        .flatMap(files -> files.getResolvedConfiguration().getResolvedArtifacts().stream())
                        .map(resolvedArtifact -> resolvedArtifact.getId().getComponentIdentifier())
                        .filter(ModuleComponentIdentifier.class::isInstance)
                        .map(ModuleComponentIdentifier.class::cast)
                        .map(moduleComponentIdentifier -> {
                            String group = moduleComponentIdentifier.getGroup();
                            String artifact = moduleComponentIdentifier.getModule();
                            String version = moduleComponentIdentifier.getVersion();

                            String wellKnownLink = findWellKnownLink(group, artifact, version);
                            if (wellKnownLink != null) {
                                javadoc.getLogger().info("Using well known link '{}' for '{}:{}:{}'", wellKnownLink, group, artifact, version);
                                return wellKnownLink;
                            } else {
                                String javadocIoLink = String.format("https://static.javadoc.io/%s/%s/%s/", group, artifact, version);
                                if (checkLink(javadocIoLink)) {
                                    javadoc.getLogger().info("Using javadoc.io link for '{}:{}:{}'", group, artifact, version);
                                    return javadocIoLink;
                                } else {
                                    return null;
                                }
                            }
                        })
                        .filter(Objects::nonNull)
                        .forEach(link -> addLink(javadoc, link));

                boolean usedJavadocIo = getLinks(javadoc).stream()
                        .anyMatch(link -> link.startsWith("https://static.javadoc.io"));

                if (usedJavadocIo && javadoc.getOptions().getJFlags().stream().noneMatch(flag -> flag.contains("http.agent"))) {
                    javadoc.getOptions().jFlags("-Dhttp.agent=" + System.currentTimeMillis());
                }
            });
        });
    }

    @SneakyThrows
    private boolean checkLink(String link) {
        Request request = new Request.Builder()
                .url(link + "package-list")
                .head()
                .header("User-Agent", String.valueOf(System.currentTimeMillis()))
                .build();

        Response response = okHttpClient.newCall(request).execute();

        return response.isSuccessful();
    }

    private void addLink(Javadoc javadoc, String link) {
        MinimalJavadocOptions options = javadoc.getOptions();
        if (options instanceof StandardJavadocDocletOptions) {
            StandardJavadocDocletOptions docletOptions = (StandardJavadocDocletOptions) options;
            Logger logger = javadoc.getLogger();
            List<String> links = docletOptions.getLinks();

            if (links == null || !links.contains(link)) {
                logger.debug("Adding '{}' to {}", link, javadoc);
                docletOptions.links(link);
            } else {
                logger.info("Not adding '{}' to {} because it's already present", link, javadoc);
            }
        }
    }

    private List<String> getLinks(Javadoc javadoc) {
        MinimalJavadocOptions options = javadoc.getOptions();
        if (options instanceof StandardJavadocDocletOptions) {
            return ((StandardJavadocDocletOptions) options).getLinks();
        }
        return Collections.emptyList();
    }

    private String findWellKnownLink(String group, String artifact, String version) {

        if (group.equals("javax") && artifact.equals("javaee-api") && version.matches("[567]\\..*")) {
            return "https://docs.oracle.com/javaee/" + version.substring(0, 1) + "/api/";
        }

        if (group.equals("javax") && artifact.equals("javaee-api") && version.startsWith("8")) {
            return "https://javaee.github.io/javaee-spec/javadocs/";
        }

        if (group.equals("org.springframework") && artifact.startsWith("spring-")) {
            return "https://docs.spring.io/spring/docs/" + version + "/javadoc-api/";
        }

        if (group.equals("org.springframework.boot") && artifact.startsWith("spring-boot")) {
            return "https://docs.spring.io/spring-boot/docs/" + version + "/api/";
        }

        if (group.equals("org.springframework.security") && artifact.startsWith("spring-security")) {
            return "https://docs.spring.io/spring-security/site/docs/" + version + "/api/";
        }

        if (group.equals("org.springframework.data") && artifact.equals("spring-data-jpa")) {
            return "https://docs.spring.io/spring-data/jpa/docs/" + version + "/api/";
        }

        if (group.equals("org.springframework.webflow") && artifact.equals("spring-webflow")) {
            return "https://docs.spring.io/spring-webflow/docs/" + version + "/api/";
        }

        if (group.equals("com.squareup.okio") && version.startsWith("1.")) {
            return "https://square.github.io/okio/1.x/" + artifact + "/";
        }

        if (group.equals("com.squareup.okhttp3")) {
            return "https://square.github.io/okhttp/3.x/" + artifact + "/";
        }

        if (group.equals("com.squareup.retrofit")) {
            return "https://square.github.io/retrofit/1.x/retrofit/";
        }

        if (group.equals("com.squareup.retrofit2")) {
            return "https://square.github.io/retrofit/2.x/" + artifact + "/";
        }

        if (group.equals("org.hibernate") && artifact.equals("hibernate-core")) {
            return "https://docs.jboss.org/hibernate/orm/" + version.substring(0, 3) + "/javadocs/";
        }

        if ((group.equals("org.hibernate") || group.equals("org.hibernate.validator")) && artifact.equals("hibernate-validator")) {
            return "https://docs.jboss.org/hibernate/validator/" + version.substring(0, 3) + "/api/";
        }

        return null;
    }

    private Stream<Configuration> findConfigurations(FileCollection classpath) {
        if (classpath instanceof Configuration) {
            return Stream.of((Configuration) classpath);
        } else if (classpath instanceof UnionFileCollection) {
            return ((UnionFileCollection) classpath).getSources()
                    .stream()
                    .flatMap(this::findConfigurations);
        }
        return Stream.empty();
    }

    private String getJavaSeLink(JavaVersion javaVersion) {
        if (javaVersion.isJava11Compatible()) {
            return "https://docs.oracle.com/en/java/javase/" + javaVersion.getMajorVersion() + "/docs/api/";
        } else {
            return "https://docs.oracle.com/javase/" + javaVersion.getMajorVersion() + "/docs/api/";
        }
    }
}
