package io.freefair.gradle.plugins.build;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.TaskProvider;

public abstract class DefaultVersionsClassPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getPlugins().apply("java");

        TaskProvider<GenerateStaticFieldsClass> register = project.getTasks().register("generateDefaultVersionsClass", GenerateStaticFieldsClass.class, task -> {
            task.getPackageName().convention(project.provider(() -> "io.freefair.gradle.plugins." + project.getName().replace("-plugin", "")));
            task.getClassName().convention("DefaultVersions");
            task.getDestinationDirectory().convention(project.getLayout().getBuildDirectory().dir("generated/sources/default-versions/java/main"));
        });

        project.getExtensions().getByType(JavaPluginExtension.class)
                .getSourceSets()
                .getByName("main")
                .getJava()
                .srcDir(register);

    }
}
