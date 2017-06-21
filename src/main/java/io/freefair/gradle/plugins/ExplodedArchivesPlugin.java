package io.freefair.gradle.plugins;

import io.freefair.gradle.plugins.base.AbstractExtensionPlugin;
import org.gradle.api.Action;
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
public class ExplodedArchivesPlugin extends AbstractExtensionPlugin<ExplodedArchivesExtension> {

    private Task explodeAll;

    @Override
    public void apply(final Project project) {
        super.apply(project);

        explodeAll = project.getTasks().create("explode");
        explodeAll.setGroup(BasePlugin.BUILD_GROUP);

        project.getTasks().withType(AbstractArchiveTask.class, new Action<AbstractArchiveTask>() {
            @Override
            public void execute(final AbstractArchiveTask archiveTask) {
                Sync explodeArchive = project.getTasks().create("explode" + capitalize((CharSequence) archiveTask.getName()), Sync.class);

                explodeAll.dependsOn(explodeArchive);
                explodeArchive.dependsOn(archiveTask);

                explodeArchive.getConventionMapping()
                        .map("group", new Callable<String>() {
                            @Override
                            public String call() throws Exception {
                                return archiveTask.getGroup();
                            }
                        });

                explodeArchive.getConventionMapping()
                        .map("destinationDir", new Callable<File>() {
                            @Override
                            public File call() throws Exception {
                                File explodedDir = new File(archiveTask.getDestinationDir(), "exploded");
                                if (extension.isIncludeExtension()) {
                                    return new File(explodedDir, archiveTask.getArchiveName());
                                } else {
                                    return new File(explodedDir, minus((CharSequence) archiveTask.getArchiveName(), "." + archiveTask.getExtension()));
                                }
                            }
                        });

                explodeArchive.from(new Callable<FileTree>() {
                    @Override
                    public FileTree call() throws Exception {
                        return project.zipTree(archiveTask.getArchivePath());
                    }
                });
            }
        });
    }

    @Override
    protected Class<ExplodedArchivesExtension> getExtensionClass() {
        return ExplodedArchivesExtension.class;
    }
}
