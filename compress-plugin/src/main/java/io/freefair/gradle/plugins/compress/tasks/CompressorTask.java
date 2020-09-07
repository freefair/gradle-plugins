package io.freefair.gradle.plugins.compress.tasks;

import lombok.Getter;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;

@SuppressWarnings("UnstableApiUsage")
public abstract class CompressorTask<P extends CompressorWorkParameters> extends SourceTask {

    private final WorkerExecutor workerExecutor;

    @Getter
    @OutputDirectory
    private final DirectoryProperty destinationDir = getProject().getObjects().directoryProperty();

    @Getter
    @Input
    private final Property<String> fileExtension = getProject().getObjects().property(String.class);

    @Inject
    protected CompressorTask(WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor;
    }

    @TaskAction
    public void compressFiles() {

        getProject().delete(getDestinationDir());

        WorkQueue workQueue = workerExecutor.noIsolation();

        getSource().visit(fileVisitDetails -> {
            if (!fileVisitDetails.isDirectory()) {
                workQueue.submit(getWorkAction(), parameters -> {
                    parameters.getSourceFile().set(fileVisitDetails.getFile());
                    parameters.getTargetFile().set(
                            new File(destinationDir.getAsFile().get(), fileVisitDetails.getPath() + "." + fileExtension.get())
                    );
                    fillParameters(parameters);
                });
            }
        });
    }

    @Internal
    protected abstract Class<? extends WorkAction<P>> getWorkAction();

    protected void fillParameters(P parameters) {

    }

}
