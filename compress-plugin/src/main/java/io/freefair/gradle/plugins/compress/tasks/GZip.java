package io.freefair.gradle.plugins.compress.tasks;

import lombok.Getter;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

import java.io.BufferedOutputStream;
import java.io.IOException;

@Getter
public abstract class GZip extends CompressorTask<GZip.GZipWorkParameters> {

    @Input
    @Optional
    public abstract Property<Integer> getCompressionLevel();

    @Input
    @Optional
    public abstract Property<String> getComment();

    @Input
    public abstract Property<Boolean> getAddFilename();

    public GZip() {
        getFileExtension().convention("gz");
    }

    @Override
    protected Class<GZipWorkAction> getWorkAction() {
        return GZipWorkAction.class;
    }

    @Override
    protected void fillParameters(GZipWorkParameters parameters) {
        parameters.getCompressionLevel().set(getCompressionLevel());
        parameters.getComment().set(getComment());
        parameters.getAddFilename().set(getAddFilename());
    }

    public interface GZipWorkParameters extends CompressorWorkParameters {
        Property<Integer> getCompressionLevel();

        Property<String> getComment();

        Property<Boolean> getAddFilename();
    }

    public static abstract class GZipWorkAction extends CompressorWorkAction<GzipCompressorOutputStream, GZipWorkParameters> {

        @Override
        protected GzipCompressorOutputStream createOutputStream(BufferedOutputStream outputStream) throws IOException {
            GzipParameters parameters = new GzipParameters();

            if (getParameters().getCompressionLevel().isPresent()) {
                parameters.setCompressionLevel(getParameters().getCompressionLevel().get());
            }

            if (getParameters().getComment().isPresent()) {
                parameters.setComment(getParameters().getComment().get());
            }

            if (getParameters().getAddFilename().getOrElse(false)) {
                parameters.setFileName(getParameters().getSourceFile().getAsFile().get().getName());
            }

            return new GzipCompressorOutputStream(outputStream, parameters);
        }
    }
}
