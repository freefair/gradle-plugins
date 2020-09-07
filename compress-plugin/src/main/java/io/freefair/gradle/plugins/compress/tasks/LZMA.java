package io.freefair.gradle.plugins.compress.tasks;

import lombok.Getter;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.BufferedOutputStream;
import java.io.IOException;

@SuppressWarnings("UnstableApiUsage")
@Getter
public class LZMA extends CompressorTask<CompressorWorkParameters> {

    @Inject
    public LZMA(WorkerExecutor workerExecutor) {
        super(workerExecutor);
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
