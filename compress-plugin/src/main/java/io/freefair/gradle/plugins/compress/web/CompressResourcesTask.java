package io.freefair.gradle.plugins.compress.web;

import io.freefair.gradle.plugins.compress.brotli4j.Brotli4jWorkAction;
import io.freefair.gradle.plugins.compress.brotli4j.Brotli4jTask;
import io.freefair.gradle.plugins.compress.tasks.GZip;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;
import java.io.File;

@CacheableTask
public abstract class CompressResourcesTask extends SourceTask implements Brotli4jTask {

    @Inject
    protected abstract WorkerExecutor getWorkerExecutor();

    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @Input
    @Optional
    public abstract Property<Integer> getMinSize();

    @Input
    @Optional
    public abstract Property<Double> getMinCompressionRatio();

    @Input
    @Optional
    public abstract Property<Boolean> getEnableBrotli();

    @Input
    @Optional
    public abstract Property<Boolean> getEnableGzip();

    @InputFiles
    @SkipWhenEmpty
    @IgnoreEmptyDirectories
    @PathSensitive(PathSensitivity.RELATIVE)
    public @NonNull FileTree getSource() {
        return super.getSource();
    }

    @TaskAction
    public void compressResources() {

        getFileSystemOperations().delete(deleteSpec -> deleteSpec.delete(getOutputDir()));

        WorkQueue workQueue = getWorkerExecutor().noIsolation();

        WorkQueue brotliWorkQueue = getWorkerExecutor().processIsolation(pi -> {
            pi.getClasspath().from(getBrotli4jClasspath());
            pi.getForkOptions().setMinHeapSize("8m");
            pi.getForkOptions().setMaxHeapSize("32m");
        });

        getSource().visit(fileVisitDetails -> {

            if (fileVisitDetails.isDirectory()) {
                return;
            }

            if (fileVisitDetails.getFile().length() < getMinSize().getOrElse(0)) {
                getLogger().info("Skipping resource: {}", fileVisitDetails.getRelativePath());
                return;
            }

            getLogger().info("Compressing resource: {}", fileVisitDetails.getRelativePath());

            if (getEnableGzip().getOrElse(true)) {
                File gzFile = new File(getOutputDir().get().getAsFile(), fileVisitDetails.getPath() + ".gz");

                workQueue.submit(GZip.GZipWorkAction.class, gZipWorkParameters -> {
                    gZipWorkParameters.getSourceFile().set(fileVisitDetails.getFile());
                    gZipWorkParameters.getTargetFile().set(gzFile);
                    gZipWorkParameters.getCompressionLevel().set(9);
                    gZipWorkParameters.getAddFilename().set(false);
                    gZipWorkParameters.getOnlyIfMinCompressionRatio().set(getMinCompressionRatio().orElse(1.1d));
                });
            }

            if (getEnableBrotli().getOrElse(true)) {
                File brFile = new File(getOutputDir().get().getAsFile(), fileVisitDetails.getPath() + ".br");

                brotliWorkQueue.submit(Brotli4jWorkAction.class, brotliWorkParameters -> {
                    brotliWorkParameters.getSourceFile().set(fileVisitDetails.getFile());
                    brotliWorkParameters.getTargetFile().set(brFile);
                    brotliWorkParameters.getBest().set(true);
                    brotliWorkParameters.getOnlyIfMinCompressionRatio().set(getMinCompressionRatio().orElse(1.1d));
                });

            }


        });
    }
}
