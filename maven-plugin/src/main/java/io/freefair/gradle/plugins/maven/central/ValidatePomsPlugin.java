package io.freefair.gradle.plugins.maven.central;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.publish.maven.tasks.GenerateMavenPom;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

/**
 * @author Lars Grefer
 */
public class ValidatePomsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getTasks().withType(GenerateMavenPom.class, generateMavenPom -> {

            String checkTaskName;

            if (generateMavenPom.getName().startsWith("generate")) {
                checkTaskName = "validate" + generateMavenPom.getName().substring(8);
            }
            else {
                checkTaskName = "validate" + generateMavenPom.getName();
            }

            TaskProvider<ValidateMavenPom> validateMavenPom = project.getTasks().register(checkTaskName, ValidateMavenPom.class);

            project.getPlugins().withType(LifecycleBasePlugin.class, lbp -> {
                project.getTasks().named(LifecycleBasePlugin.CHECK_TASK_NAME).configure(check -> {
                    check.dependsOn(validateMavenPom);
                });
            });

            project.afterEvaluate(p -> {
                validateMavenPom.configure(v -> {
                    v.dependsOn(generateMavenPom);
                    v.getPomFile().set(generateMavenPom.getDestination());
                });
            });
        });
    }
}
