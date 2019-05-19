package io.freefair.gradle.plugins.maven.javadoc;

import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.javadoc.Javadoc;

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
            javadoc.doFirst("resolveJavadocLinks", new ResolveJavadocLinks(okHttpClient));
        });
    }

}
