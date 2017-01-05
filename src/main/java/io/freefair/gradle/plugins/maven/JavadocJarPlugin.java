package io.freefair.gradle.plugins.maven;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.javadoc.Javadoc;

/**
 * @author Lars Grefer
 * @see <a href="http://stackoverflow.com/a/11475089">http://stackoverflow.com/a/11475089</a>
 */
public class JavadocJarPlugin extends AbstractMavenJarPlugin {

    @Override
    public void apply(final Project project) {
        super.apply(project);

        getJarTask().setDescription("Assembles a jar archive containing the javadocs.");

        project.getPluginManager().withPlugin("java", new Action<AppliedPlugin>() {
            @Override
            public void execute(AppliedPlugin appliedPlugin) {
                Javadoc javadocTask = (Javadoc) project.getTasks().getByName(JavaPlugin.JAVADOC_TASK_NAME);

                getJarTask().dependsOn(javadocTask);
                getJarTask().from(javadocTask.getDestinationDir());
            }
        });
    }

    @Override
    protected String getClassifier() {
        return "javadoc";
    }
}
