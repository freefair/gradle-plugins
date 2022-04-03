package io.freefair.gradle.plugins.maven.javadoc;

import io.freefair.gradle.plugins.maven.version.ParsingHelper;
import io.freefair.gradle.plugins.maven.version.Version;
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
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.io.IOException;
import java.util.*;

@NonNullApi
public class ResolveJavadocLinks {

    private final OkHttpClient okHttpClient;
    private final ServiceLoader<JavadocLinkProvider> javadocLinkProviders;

    private Javadoc javadoc;
    private Logger logger;

    public ResolveJavadocLinks(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
        javadocLinkProviders = ServiceLoader.load(JavadocLinkProvider.class, this.getClass().getClassLoader());
    }

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
        IOException exception = null;

        for (String file : Arrays.asList("element-list", "package-list")) {
            Request request = new Request.Builder()
                    .url(link + file)
                    .get()
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return true;
                }
            } catch (IOException e) {
                logger.info("Failed to access {}", request.url(), e);
                if (exception == null) {
                    exception = e;
                }
                else {
                    exception.addSuppressed(e);
                }
            }
        }

        if (exception != null) {
            logger.warn("Failed to check link {}", link, exception);
        }

        return false;
    }

    @Nullable
    private String findWellKnownLink(String group, String artifact, String version) {

        Version v = ParsingHelper.parseVersion(version);
        javadocLinkProviders.reload();
        for (JavadocLinkProvider javadocLinkProvider : javadocLinkProviders) {
            logger.warn("{}", javadocLinkProvider.getClass());
            String javadocLink = javadocLinkProvider.getJavadocLink(group, artifact, v);
            if (javadocLink != null) {
                return javadocLink;
            }
        }

        if (group.equals("javax") && artifact.equals("javaee-api") && version.matches("[567]\\..*")) {
            return "https://docs.oracle.com/javaee/" + version.substring(0, 1) + "/api/";
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
