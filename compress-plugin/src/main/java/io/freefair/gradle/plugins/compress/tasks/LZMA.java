package io.freefair.gradle.plugins.compress.tasks;

import lombok.Getter;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;

import java.io.BufferedOutputStream;
import java.io.IOException;

@Getter
public abstract class LZMA extends CompressorTask<CompressorWorkParameters> {

    public LZMA() {
        getFileExtension().convention("lzma");
    }

    @Override
    protected Class<LZMAWorkAction> getWorkAction() {
        return LZMAWorkAction.class;
    }

    public static abstract class LZMAWorkAction extends CompressorWorkAction<LZMACompressorOutputStream, CompressorWorkParameters> {

        @Override
        protected LZMACompressorOutputStream createOutputStream(BufferedOutputStream outputStream) throws IOException {
            return new LZMACompressorOutputStream(outputStream);
        }
    }
}
