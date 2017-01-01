package io.freefair.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

/**
 * @author Lars Grefer
 * @see <a href="http://stackoverflow.com/a/11475089">http://stackoverflow.com/a/11475089</a>
 */
@SuppressWarnings("GroovyUnusedDeclaration")
class SourcesJarPlugin extends AbstractMavenJarPlugin implements Plugin<Project> {

    SourcesJarPlugin() {
        super("sourcesJar", "sources")
    }

    @Override
    void apply(Project project) {
        super.apply(project)

        project.pluginManager.withPlugin("java") {
            getJarTask().dependsOn project.tasks.getByName(JavaPlugin.CLASSES_TASK_NAME)
            getJarTask().from project.sourceSets.main.allSource
        }

    }
}
