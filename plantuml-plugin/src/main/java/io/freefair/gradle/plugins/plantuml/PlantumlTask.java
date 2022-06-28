package io.freefair.gradle.plugins.plantuml;

import lombok.Getter;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;

/**
 * @author Lars Grefer
 */
public class PlantumlTask extends SourceTask {

    private final WorkerExecutor workerExecutor;

    private final FileSystemOperations fileSystemOperations;

    @Getter
    @Classpath
    private final ConfigurableFileCollection plantumlClasspath = getProject().files();

    @Getter
    @OutputDirectory
    private final DirectoryProperty outputDirectory = getProject().getObjects().directoryProperty();

    @Getter
    @Input
    @Optional
    private final Property<String> fileFormat = getProject().getObjects().property(String.class);

    @Getter
    @Input
    private final Property<Boolean> withMetadata = getProject().getObjects().property(Boolean.class).convention(true);

    @Getter
    @Input
    private final Property<String> includePattern = getProject().getObjects().property(String.class).convention("**/*.puml");

    @Getter
    @Input
    private final Property<Boolean> deleteOutputBeforeBuild = getProject().getObjects().property(Boolean.class).convention(true);

    @Inject
    public PlantumlTask(WorkerExecutor workerExecutor, FileSystemOperations fileSystemOperations) {
        this.fileSystemOperations = fileSystemOperations;
        this.workerExecutor = workerExecutor;
        this.setGroup("plantuml");
    }

    @TaskAction
    public void execute() {

        if(deleteOutputBeforeBuild.get()) {
            fileSystemOperations.delete(deleteSpec -> deleteSpec.delete(outputDirectory));
        }

        WorkQueue workQueue = workerExecutor.processIsolation(process -> {
            process.getClasspath().from(plantumlClasspath);
            process.getForkOptions().systemProperty("java.awt.headless", true);
        });

        for (File file : getSource().matching(p -> p.include(includePattern.get()))) {
            workQueue.submit(PlantumlAction.class, params -> {
                params.getInputFile().set(file);
                params.getOutputDirectory().set(outputDirectory);
                params.getFileFormat().set(fileFormat);
                params.getWithMetadata().set(withMetadata);
            });
        }
    }
}
