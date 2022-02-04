package io.freefair.gradle.plugins.compress.tasks;

import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.gradle.workers.WorkAction;

import java.io.*;

@SuppressWarnings("UnstableApiUsage")
public abstract class CompressorWorkAction<OS extends CompressorOutputStream, P extends CompressorWorkParameters> implements WorkAction<P> {

    @Override
    public void execute() {

        File outputFile = getParameters().getTargetFile().getAsFile().get();
        File outputDir = outputFile.getParentFile();

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        if (outputDir.exists() || outputDir.mkdirs()) {
            try (
                    InputStream in = new FileInputStream(getParameters().getSourceFile().getAsFile().get());
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
                    OS compressorStream = createOutputStream(out)
            ) {
                IOUtils.copy(in, compressorStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            throw new RuntimeException(outputDir + " could not be created");
        }
    }

    protected abstract OS createOutputStream(BufferedOutputStream outputStream) throws IOException;
}
