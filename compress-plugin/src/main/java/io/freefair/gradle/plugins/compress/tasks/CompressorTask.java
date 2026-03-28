package io.freefair.gradle.plugins.compress.tasks;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;

@CacheableTask
public abstract class CompressorTask<P extends CompressorWorkParameters> extends SourceTask {

    @Inject
    protected abstract WorkerExecutor getWorkerExecutor();

    @OutputDirectory
    public abstract DirectoryProperty getDestinationDir();

    @Input
    public abstract Property<String> getFileExtension();

    @TaskAction
    public void compressFiles() {

        getProject().delete(getDestinationDir());

        WorkQueue workQueue = getWorkerExecutor().noIsolation();

        getSource().visit(fileVisitDetails -> {
            if (!fileVisitDetails.isDirectory()) {
                workQueue.submit(getWorkAction(), parameters -> {
                    parameters.getSourceFile().set(fileVisitDetails.getFile());
                    parameters.getTargetFile().set(
                            new File(getDestinationDir().getAsFile().get(), fileVisitDetails.getPath() + "." + getFileExtension().get())
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
