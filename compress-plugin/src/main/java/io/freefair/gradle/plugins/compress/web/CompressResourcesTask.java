package io.freefair.gradle.plugins.compress.web;

import io.freefair.gradle.plugins.compress.BrotliWorkAction;
import io.freefair.gradle.plugins.compress.tasks.GZip;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;

public abstract class CompressResourcesTask extends SourceTask {

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @Input
    @Optional
    public abstract Property<Integer> getMinSize();

    @Inject
    protected abstract WorkerExecutor getWorkerExecutor();

    @TaskAction
    public void compressResources() {

        WorkQueue workQueue = getWorkerExecutor().noIsolation();

        getSource().visit(fileVisitDetails -> {

            if (fileVisitDetails.isDirectory()) {
                return;
            }

            if (fileVisitDetails.getFile().length() < getMinSize().getOrElse(0)) {
                getLogger().info("Skipping resource: {}", fileVisitDetails.getRelativePath());
                return;
            }

            getLogger().info("Compressing resource: {}", fileVisitDetails.getRelativePath());

            File gzFile = new File(getOutputDir().get().getAsFile(), fileVisitDetails.getPath() + ".gz");
            File brFile = new File(getOutputDir().get().getAsFile(), fileVisitDetails.getPath() + ".br");

            workQueue.submit(GZip.GZipWorkAction.class, gZipWorkParameters -> {
                gZipWorkParameters.getSourceFile().set(fileVisitDetails.getFile());
                gZipWorkParameters.getTargetFile().set(gzFile);
                gZipWorkParameters.getCompressionLevel().set(9);
                gZipWorkParameters.getAddFilename().set(false);
                gZipWorkParameters.getOnlyIfMinCompressionRatio().set(1.1d);
            });

            workQueue.submit(BrotliWorkAction.class, brotliWorkParameters -> {
                brotliWorkParameters.getSourceFile().set(fileVisitDetails.getFile());
                brotliWorkParameters.getTargetFile().set(brFile);
                brotliWorkParameters.getBest().set(true);
                brotliWorkParameters.getOnlyIfMinCompressionRatio().set(1.1d);
            });

        });
    }
}
