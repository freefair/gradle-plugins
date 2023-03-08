package io.freefair.gradle.plugins.maven.javadoc;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.api.DefaultTask;
import org.gradle.api.JavaVersion;
import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.MinimalJavadocOptions;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavadocTool;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

@NonNullApi
public abstract class ResolveJavadocLinks extends DefaultTask {

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @Nested
    public abstract Property<JavadocTool> getJavadocTool();

    @Input
    public abstract ListProperty<ComponentArtifactIdentifier> getArtifactIds();

    private final OkHttpClient okHttpClient;
    private final ServiceLoader<JavadocLinkProvider> javadocLinkProviders;

    public void setClasspath(Configuration configuration) {
        Provider<Set<ResolvedArtifactResult>> resolvedArtifacts = configuration.getIncoming().getArtifacts().getResolvedArtifacts();
        getArtifactIds().set(resolvedArtifacts.map(artifacts -> artifacts.stream().map(ResolvedArtifactResult::getId).collect(Collectors.toList())));
    }

    @Inject
    public ResolveJavadocLinks(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
        javadocLinkProviders = ServiceLoader.load(JavadocLinkProvider.class, this.getClass().getClassLoader());
    }

    @TaskAction
    public void resolveLinks() throws IOException {

        LinkedHashSet<String> links = new LinkedHashSet<>();

        links.add(getJavaSeLink());

        getArtifactIds().get()
                .stream()
                .map(ComponentArtifactIdentifier::getComponentIdentifier)
                .filter(ModuleComponentIdentifier.class::isInstance)
                .map(ModuleComponentIdentifier.class::cast)
                .map(moduleComponentIdentifier -> {
                    String group = moduleComponentIdentifier.getGroup();
                    String artifact = moduleComponentIdentifier.getModule();
                    String version = moduleComponentIdentifier.getVersion();

                    String wellKnownLink = findWellKnownLink(group, artifact, version);
                    if (wellKnownLink != null) {
                        getLogger().info("Using well known link '{}' for '{}:{}:{}'", wellKnownLink, group, artifact, version);
                        return wellKnownLink;
                    }
                    else {
                        String javadocIoLink = String.format("https://www.javadoc.io/doc/%s/%s/%s/", group, artifact, version);
                        if (checkLink(javadocIoLink)) {
                            getLogger().info("Using javadoc.io link for '{}:{}:{}'", group, artifact, version);
                            return javadocIoLink;
                        }
                        else {
                            return null;
                        }
                    }
                })
                .filter(Objects::nonNull)
                .forEach(links::add);

        try (PrintWriter printWriter = ResourceGroovyMethods.newPrintWriter(getOutputFile().get().getAsFile())) {
            for (String link : links) {
                printWriter.printf("-link '%s'%n", link);
            }
        }
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
                getLogger().info("Failed to access {}", request.url(), e);
                if (exception == null) {
                    exception = e;
                }
                else {
                    exception.addSuppressed(e);
                }
            }
        }

        if (exception != null) {
            getLogger().warn("Failed to check link {}", link, exception);
        }

        return false;
    }

    @Nullable
    private String findWellKnownLink(String group, String artifact, String version) {

        javadocLinkProviders.reload();
        for (JavadocLinkProvider javadocLinkProvider : javadocLinkProviders) {
            getLogger().info("{}", javadocLinkProvider.getClass());
            String javadocLink = javadocLinkProvider.getJavadocLink(group, artifact, version);
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

        if (group.equals("org.projectlombok")) {
            return "https://projectlombok.org/api/";
        }

        if (group.equals("org.jetbrains.kotlin")) {
            return "https://kotlinlang.org/api/latest/jvm/stdlib/";
        }

        return null;
    }




    private String getJavaSeLink() {
        JavaVersion javaVersion = JavaVersion.current();

        if (getJavadocTool().isPresent()) {
            JavaLanguageVersion languageVersion = getJavadocTool().get().getMetadata().getLanguageVersion();
            javaVersion = JavaVersion.toVersion(languageVersion.asInt());
        }

        if (javaVersion.isJava11Compatible()) {
            return "https://docs.oracle.com/en/java/javase/" + javaVersion.getMajorVersion() + "/docs/api/";
        }
        else {
            return "https://docs.oracle.com/javase/" + javaVersion.getMajorVersion() + "/docs/api/";
        }
    }

}
