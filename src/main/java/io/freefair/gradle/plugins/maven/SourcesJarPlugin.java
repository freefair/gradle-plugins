package io.freefair.gradle.plugins.maven;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.internal.tasks.DefaultSourceSet;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

/**
 * @author Lars Grefer
 * @see <a href="http://stackoverflow.com/a/11475089">http://stackoverflow.com/a/11475089</a>
 */
public class SourcesJarPlugin extends AbstractMavenJarPlugin{

    @Override
    public void apply(final Project project) {
        super.apply(project);

        getJarTask().setDescription("Assembles a jar archive containing the sources.");

        project.getPluginManager().withPlugin("java", new Action<AppliedPlugin>() {
            @Override
            public void execute(AppliedPlugin appliedPlugin) {
                getJarTask().dependsOn(project.getTasks().getByName(JavaPlugin.CLASSES_TASK_NAME));

                JavaPluginConvention javaPluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
                DefaultSourceSet mainSourceSet = (DefaultSourceSet) javaPluginConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
                getJarTask().from(mainSourceSet.getAllSource());
            }
        });
    }

    @Override
    protected String getClassifier() {
        return "sources";
    }
}
