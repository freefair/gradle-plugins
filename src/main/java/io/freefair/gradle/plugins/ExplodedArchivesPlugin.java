package io.freefair.gradle.plugins;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileTree;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.io.File;
import java.util.concurrent.Callable;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;
import static org.codehaus.groovy.runtime.StringGroovyMethods.minus;

/**
 * @author Lars Grefer
 */
@Getter
public class ExplodedArchivesPlugin implements Plugin<Project> {

    private Task explodeAll;
    private ExplodedArchivesExtension explodedArchives;

    @Override
    public void apply(final Project project) {

        explodedArchives = project.getExtensions().create("explodedArchives", ExplodedArchivesExtension.class);

        explodeAll = project.getTasks().create("explode");
        explodeAll.setGroup(BasePlugin.BUILD_GROUP);

        project.getTasks().withType(AbstractArchiveTask.class, archiveTask -> {
            Sync explodeArchive = project.getTasks().create("explode" + capitalize((CharSequence) archiveTask.getName()), Sync.class);

            explodeAll.dependsOn(explodeArchive);
            explodeArchive.dependsOn(archiveTask);

            explodeArchive.getConventionMapping()
                    .map("group", (Callable<String>) archiveTask::getGroup);

            explodeArchive.getConventionMapping()
                    .map("destinationDir", (Callable<File>) () -> {
                        File explodedDir = new File(archiveTask.getDestinationDir(), "exploded");
                        if (explodedArchives.isIncludeExtension()) {
                            return new File(explodedDir, archiveTask.getArchiveName());
                        } else {
                            return new File(explodedDir, minus((CharSequence) archiveTask.getArchiveName(), "." + archiveTask.getExtension()));
                        }
                    });

            explodeArchive.from((Callable<FileTree>) () -> project.zipTree(archiveTask.getArchivePath()));
        });
    }
}
