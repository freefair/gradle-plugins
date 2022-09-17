package io.freefair.gradle.plugins.compress.tasks;

import lombok.Getter;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream;
import org.apache.commons.compress.compressors.deflate.DeflateParameters;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

import java.io.BufferedOutputStream;
import java.io.IOException;

@Getter
public abstract class Deflate extends CompressorTask<Deflate.DeflateWorkParameters> {

    @Input
    @Optional
    public abstract Property<Integer> getCompressionLevel();

    @Input
    @Optional
    public abstract Property<Boolean> getWithZlibHeader();

    public Deflate() {
        getFileExtension().convention("deflate");
    }

    @Override
    protected Class<DeflateWorkAction> getWorkAction() {
        return DeflateWorkAction.class;
    }

    @Override
    protected void fillParameters(DeflateWorkParameters parameters) {
        parameters.getCompressionLevel().set(getCompressionLevel());
        parameters.getWithZlibHeader().set(getWithZlibHeader());
    }

    public interface DeflateWorkParameters extends CompressorWorkParameters {
        Property<Integer> getCompressionLevel();

        Property<Boolean> getWithZlibHeader();
    }

    public static abstract class DeflateWorkAction extends CompressorWorkAction<DeflateCompressorOutputStream, DeflateWorkParameters> {

        @Override
        protected DeflateCompressorOutputStream createOutputStream(BufferedOutputStream outputStream) throws IOException {
            DeflateParameters parameters = new DeflateParameters();

            if (getParameters().getCompressionLevel().isPresent()) {
                parameters.setCompressionLevel(getParameters().getCompressionLevel().get());
            }

            if (getParameters().getWithZlibHeader().isPresent()) {
                parameters.setWithZlibHeader(getParameters().getWithZlibHeader().get());
            }

            return new DeflateCompressorOutputStream(outputStream, parameters);
        }
    }
}
