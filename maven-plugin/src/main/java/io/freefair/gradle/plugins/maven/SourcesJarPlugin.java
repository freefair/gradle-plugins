package io.freefair.gradle.plugins.maven;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.internal.tasks.DefaultSourceSet;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;

/**
 * @author Lars Grefer
 * @see <a href="http://stackoverflow.com/a/11475089">http://stackoverflow.com/a/11475089</a>
 */
@Getter
public class SourcesJarPlugin implements Plugin<Project> {

    private TaskProvider<Jar> sourcesJar;

    @Override
    public void apply(Project project) {
        project.getPluginManager().withPlugin("java", appliedPlugin -> {
            sourcesJar = project.getTasks().register("sourcesJar", Jar.class, sourcesJar -> {
                sourcesJar.setDescription("Assembles a jar archive containing the sources.");
                sourcesJar.getArchiveClassifier().set("sources");
                sourcesJar.setGroup(BasePlugin.BUILD_GROUP);

                sourcesJar.dependsOn(project.getTasks().named(JavaPlugin.CLASSES_TASK_NAME));

                JavaPluginConvention javaPluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
                DefaultSourceSet mainSourceSet = (DefaultSourceSet) javaPluginConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
                sourcesJar.from(mainSourceSet.getAllSource());
            });

            project.getArtifacts().add(Dependency.ARCHIVES_CONFIGURATION, sourcesJar);
        });
    }

}
