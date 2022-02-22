package io.freefair.gradle.plugins.maven.javadoc;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.javadoc.Javadoc;

class AggregateJavadocClientPlugin implements Plugin<Project> {

    @Getter
    private ConfigurableFileCollection javadocClasspath;

    @Getter
    private TaskProvider<Task> collectJavadocClasspath;

    @Override
    public void apply(Project project) {
        collectJavadocClasspath = project.getTasks().register("collectJavadocClasspath");
        javadocClasspath = project.files().builtBy(collectJavadocClasspath);

        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
            collectJavadocClasspath.configure(c -> {
                Javadoc javadoc = (Javadoc) project.getTasks().getByName(JavaPlugin.JAVADOC_TASK_NAME);
                c.setEnabled(javadoc.isEnabled());
                c.doFirst(t -> {
                    javadocClasspath
                            .from(javadoc.getClasspath().getFiles())
                            .builtBy(javadoc.getClasspath());
                });
            });
        });
    }
}
