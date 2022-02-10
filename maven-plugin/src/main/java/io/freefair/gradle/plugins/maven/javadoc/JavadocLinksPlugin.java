package io.freefair.gradle.plugins.maven.javadoc;

import io.freefair.gradle.plugins.okhttp.OkHttpPlugin;
import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.javadoc.Javadoc;

@Getter
public class JavadocLinksPlugin implements Plugin<Project> {

    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;

        OkHttpPlugin okHttpPlugin = project.getPlugins().apply(OkHttpPlugin.class);

        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
            TaskProvider<Javadoc> javadoc = project.getTasks().named(JavaPlugin.JAVADOC_TASK_NAME, Javadoc.class);

            project.afterEvaluate(p -> {
                javadoc.configure(jd -> {
                    ResolveJavadocLinks rjd = new ResolveJavadocLinks(okHttpPlugin.getOkHttpClient());
                    rjd.resolveLinks(jd);
                });
            });
        });
    }

}
