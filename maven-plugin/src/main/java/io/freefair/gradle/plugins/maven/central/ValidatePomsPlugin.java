package io.freefair.gradle.plugins.maven.central;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.publish.maven.tasks.GenerateMavenPom;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

/**
 * @author Lars Grefer
 */
public class ValidatePomsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        TaskProvider<Task> validateAll = project.getTasks().register("validatePomFiles", t -> {
            t.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
        });

        project.getTasks().withType(GenerateMavenPom.class, generateMavenPom -> {

            String checkTaskName;

            if (generateMavenPom.getName().startsWith("generate")) {
                checkTaskName = "validate" + generateMavenPom.getName().substring(8);
            }
            else {
                checkTaskName = "validate" + generateMavenPom.getName();
            }

            TaskProvider<ValidateMavenPom> validateMavenPom = project.getTasks().register(checkTaskName, ValidateMavenPom.class, t -> {
                t.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
                String description = generateMavenPom.getDescription();
                if (description != null) {
                    t.setDescription(description.replace("Generates", "Validates"));
                }
            });

            validateAll.configure(all -> all.dependsOn(validateMavenPom));

            project.getPlugins().withType(LifecycleBasePlugin.class, lbp -> {
                project.getTasks().named(LifecycleBasePlugin.CHECK_TASK_NAME).configure(check -> {
                    check.dependsOn(validateMavenPom);
                });
            });

            validateMavenPom.configure(v -> {
                v.dependsOn(generateMavenPom);
                v.getPomFile().set(project.getLayout().file(project.provider(generateMavenPom::getDestination)));
            });
        });
    }
}
