package io.freefair.gradle.plugins;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.javadoc.Javadoc;

/**
 * @author Lars Grefer
 * @see <a href="http://stackoverflow.com/a/11475089">http://stackoverflow.com/a/11475089</a>
 */
public class JavadocJarPlugin extends AbstractMavenJarPlugin implements Plugin<Project> {

    public JavadocJarPlugin() {
        super("javadocJar", "javadoc");
    }

    @Override
    public void apply(final Project project) {
        super.apply(project);

        project.getPluginManager().withPlugin("java", new Action<AppliedPlugin>() {
            @Override
            public void execute(AppliedPlugin appliedPlugin) {
                Javadoc javadocTask = (Javadoc) project.getTasks().getByName(JavaPlugin.JAVADOC_TASK_NAME);

                getJarTask().dependsOn(javadocTask);
                getJarTask().from(javadocTask.getDestinationDir());
            }
        });
    }
}
