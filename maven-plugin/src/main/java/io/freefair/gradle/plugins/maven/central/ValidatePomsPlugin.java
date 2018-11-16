package io.freefair.gradle.plugins.maven.central;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.publish.maven.tasks.GenerateMavenPom;

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
            } else {
                checkTaskName = "validate" + generateMavenPom.getName();
            }

            ValidateMavenPom validateMavenPom = project.getTasks().create(checkTaskName, ValidateMavenPom.class);

            project.afterEvaluate(p -> {

                Task check = project.getTasks().findByName(JavaBasePlugin.CHECK_TASK_NAME);
                if (check != null) {
                    check.dependsOn(validateMavenPom);
                }

                validateMavenPom.dependsOn(generateMavenPom);
                validateMavenPom.getPomFile().set(generateMavenPom.getDestination());
            });
        });
    }
}
