package io.freefair.gradle.plugins.maven.javadoc;

import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.UnionFileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.MinimalJavadocOptions;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import java.io.IOException;
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

        project.getTasks().withType(Javadoc.class).configureEach(javadoc -> {
            addLink(javadoc, getJavaSeLink(JavaVersion.current()));

            javadoc.doFirst("resolveJavadocLinks", new ResolveJavadocIoLinks(okHttpClient));
        });
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
            }
            else {
                logger.info("Not adding '{}' to {} because it's already present", link, javadoc);
            }
        }
    }

    private String getJavaSeLink(JavaVersion javaVersion) {
        if (javaVersion.isJava11Compatible()) {
            return "https://docs.oracle.com/en/java/javase/" + javaVersion.getMajorVersion() + "/docs/api/";
        }
        else {
            return "https://docs.oracle.com/javase/" + javaVersion.getMajorVersion() + "/docs/api/";
        }
    }
}
