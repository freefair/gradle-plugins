package io.freefair.gradle.plugins.maven.javadoc;

import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.gradle.api.JavaVersion;
import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.MinimalJavadocOptions;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavadocTool;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@NonNullApi
@RequiredArgsConstructor
public class ResolveJavadocLinks {

    private final OkHttpClient okHttpClient;

    private Javadoc javadoc;
    private Logger logger;

    public void resolveLinks(Javadoc javadoc, Configuration classpath) {
        logger = javadoc.getLogger();
        this.javadoc = javadoc;

        addLink(getJavaSeLink());

        classpath.getResolvedConfiguration()
                .getResolvedArtifacts()
                .stream()
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
                    }
                    else {
                        String javadocIoLink = String.format("https://www.javadoc.io/doc/%s/%s/%s/", group, artifact, version);
                        if (checkLink(javadocIoLink)) {
                            javadoc.getLogger().info("Using javadoc.io link for '{}:{}:{}'", group, artifact, version);
                            return javadocIoLink;
                        }
                        else {
                            return null;
                        }
                    }
                })
                .filter(Objects::nonNull)
                .forEach(this::addLink);
    }

    private boolean checkLink(String link) {
        Request elementListRequest = new Request.Builder()
                .url(link + "element-list")
                .get()
                .build();

        IOException exception = null;

        try (Response response = okHttpClient.newCall(elementListRequest).execute()) {
            if (response.isSuccessful()) {
                return true;
            }
        } catch (IOException e) {
            exception = e;
        }

        Request packageListRequest = new Request.Builder()
                .url(link + "package-list")
                .get()
                .build();

        try (Response response = okHttpClient.newCall(packageListRequest).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            if (exception != null) {
                e.addSuppressed(exception);
            }
            logger.warn("Failed to access {}", packageListRequest.url(), e);
            return false;
        }
    }

    @Nullable
    private String findWellKnownLink(String group, String artifact, String version) {

        if (group.equals("javax") && artifact.equals("javaee-api") && version.matches("[567]\\..*")) {
            return "https://docs.oracle.com/javaee/" + version.substring(0, 1) + "/api/";
        }

        if (group.equals("javax") && artifact.equals("javaee-api") && version.startsWith("8")) {
            return "https://javaee.github.io/javaee-spec/javadocs/";
        }

        if (group.equals("org.springframework") && artifact.startsWith("spring-")) {
            return "https://docs.spring.io/spring-framework/docs/" + version + "/javadoc-api/";
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
            if (version.startsWith("3.")) {
                return "https://square.github.io/okhttp/3.x/" + artifact + "/";
            }
            if (version.startsWith("4.")) {
                return "https://square.github.io/okhttp/4.x/" + artifact + "/";
            }
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

        if (group.equals("org.primefaces") && artifact.equals("primefaces")) {
            return "https://www.primefaces.org/docs/api/" + version + "/";
        }

        if (group.equals("org.eclipse.jetty")) {
            return "https://www.eclipse.org/jetty/javadoc/" + version + "/";
        }

        if (group.equals("org.ow2.asm")) {
            return "https://asm.ow2.io/javadoc/";
        }

        if (group.equals("org.joinfaces")) {
            return "https://docs.joinfaces.org/" + version + "/api/";
        }

        if (group.startsWith("org.apache.tomcat")) {
            return "https://tomcat.apache.org/tomcat-" + version.substring(0, 3) + "-doc/api/";
        }

        return null;
    }


    private String getJavaSeLink() {
        JavaVersion javaVersion = JavaVersion.current();

        Property<JavadocTool> javadocTool = javadoc.getJavadocTool();
        if (javadocTool.isPresent()) {
            JavaLanguageVersion languageVersion = javadocTool.get().getMetadata().getLanguageVersion();
            javaVersion = JavaVersion.toVersion(languageVersion.asInt());
        }

        if (javaVersion.isJava11Compatible()) {
            return "https://docs.oracle.com/en/java/javase/" + javaVersion.getMajorVersion() + "/docs/api/";
        }
        else {
            return "https://docs.oracle.com/javase/" + javaVersion.getMajorVersion() + "/docs/api/";
        }
    }

    private void addLink(String link) {
        MinimalJavadocOptions options = javadoc.getOptions();
        if (options instanceof StandardJavadocDocletOptions) {
            StandardJavadocDocletOptions docletOptions = (StandardJavadocDocletOptions) options;
            List<String> links = docletOptions.getLinks();

            if (links == null || !links.contains(link)) {
                logger.debug("Adding '{}' to {}", link, javadoc);
                docletOptions.links(link);
            }
            else {
                logger.info("Not adding '{}' to {} because it's already present", link, javadoc);
            }
        }
    }
}
