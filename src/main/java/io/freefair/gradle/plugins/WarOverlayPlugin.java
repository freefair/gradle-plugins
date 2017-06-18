package io.freefair.gradle.plugins;

import io.freefair.gradle.plugins.base.AbstractExtensionPlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.bundling.War;

import java.io.File;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

/**
 * @author Lars Grefer
 */
public class WarOverlayPlugin extends AbstractExtensionPlugin<WarOverlayExtension> {

    @Override
    public void apply(final Project project) {
        super.apply(project);
        project.getPluginManager().apply(WarPlugin.class);

        project.getTasks().withType(War.class, new Action<War>() {
            @Override
            public void execute(final War warTask) {
                Task configTask = project.getTasks().create("configureOverlayFor" + capitalize((CharSequence) warTask.getName()));

                warTask.dependsOn(configTask);

                configTask.doFirst(new Action<Task>() {
                    @Override
                    public void execute(Task configTask) {
                        for (File file : project.getConfigurations().getByName(JavaPlugin.RUNTIME_CONFIGURATION_NAME)) {
                            if (file.getName().endsWith(".war")) {

                                configTask.getLogger().info("Using {} as overlay", file.getName());

                                warTask.from(project.zipTree(file), new Action<CopySpec>() {
                                    @Override
                                    public void execute(CopySpec overlayCopySpec) {
                                        overlayCopySpec.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
                                        overlayCopySpec.exclude(extension.getExcludes());
                                    }
                                });

                                warTask.getRootSpec().exclude("**/" + file.getName());
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    protected Class<WarOverlayExtension> getExtensionClass() {
        return WarOverlayExtension.class;
    }
}
