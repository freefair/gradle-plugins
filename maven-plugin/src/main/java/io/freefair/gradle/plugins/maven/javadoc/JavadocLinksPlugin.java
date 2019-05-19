package io.freefair.gradle.plugins.maven.javadoc;

import io.freefair.gradle.plugins.okhttp.OkHttpPlugin;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.javadoc.Javadoc;

@Getter
public class JavadocLinksPlugin implements Plugin<Project> {

    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;

        OkHttpPlugin okHttpPlugin = project.getPlugins().apply(OkHttpPlugin.class);

        project.getTasks().withType(Javadoc.class)
                .configureEach(javadoc -> {
                    OkHttpClient okHttpClient = okHttpPlugin.getOkHttpClient();
                    javadoc.doFirst("resolveJavadocLinks", new ResolveJavadocLinks(okHttpClient));
                });
    }

}
