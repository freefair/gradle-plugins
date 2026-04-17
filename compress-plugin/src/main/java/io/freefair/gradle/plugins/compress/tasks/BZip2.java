package io.freefair.gradle.plugins.compress.tasks;

import lombok.Getter;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.gradle.api.tasks.CacheableTask;

import java.io.BufferedOutputStream;
import java.io.IOException;

@SuppressWarnings("UnstableApiUsage")
@Getter
@CacheableTask
public abstract class BZip2 extends CompressorTask<CompressorWorkParameters> {

    public BZip2() {
        getFileExtension().convention("bz2");
    }

    @Override
    protected Class<BZip2WorkAction> getWorkAction() {
        return BZip2WorkAction.class;
    }

    public abstract static class BZip2WorkAction extends CompressorWorkAction<BZip2CompressorOutputStream, CompressorWorkParameters> {

        @Override
        protected BZip2CompressorOutputStream createOutputStream(BufferedOutputStream outputStream) throws IOException {
            return new BZip2CompressorOutputStream(outputStream);
        }
    }
}
