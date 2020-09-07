package io.freefair.gradle.plugins.compress.tasks;

import lombok.Getter;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.BufferedOutputStream;
import java.io.IOException;

@SuppressWarnings("UnstableApiUsage")
@Getter
public class GZip extends CompressorTask<GZip.GZipWorkParameters> {

    @Input
    @Optional
    private final Property<Integer> compressionLevel = getProject().getObjects().property(Integer.class);

    @Inject
    public GZip(WorkerExecutor workerExecutor) {
        super(workerExecutor);
        getFileExtension().convention("gz");
    }

    @Override
    protected Class<GZipWorkAction> getWorkAction() {
        return GZipWorkAction.class;
    }

    @Override
    protected void fillParameters(GZipWorkParameters parameters) {
        parameters.getCompressionLevel().set(getCompressionLevel());
    }

    public interface GZipWorkParameters extends CompressorWorkParameters {
        Property<Integer> getCompressionLevel();
    }

    public static abstract class GZipWorkAction extends CompressorWorkAction<GzipCompressorOutputStream, GZipWorkParameters> {

        @Override
        protected GzipCompressorOutputStream createOutputStream(BufferedOutputStream outputStream) throws IOException {
            GzipParameters parameters = new GzipParameters();
            if (getParameters().getCompressionLevel().isPresent()) {
                parameters.setCompressionLevel(getParameters().getCompressionLevel().get());
            }
            parameters.setFilename(getParameters().getSourceFile().getAsFile().get().getName());
            return new GzipCompressorOutputStream(outputStream, parameters);
        }
    }
}
