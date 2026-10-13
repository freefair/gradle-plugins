package io.freefair.gradle.plugins.compress.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.gradle.workers.WorkAction;

import java.io.*;

@Slf4j
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

            checkMinCompressionRatio(getParameters());

        } else {
            throw new RuntimeException(outputDir + " could not be created");
        }
    }

    protected abstract OS createOutputStream(BufferedOutputStream outputStream) throws IOException;

    public static void checkMinCompressionRatio(CompressorWorkParameters parameters) {

        File sourceFile = parameters.getSourceFile().get().getAsFile();
        File targetFile = parameters.getTargetFile().get().getAsFile();
        long originalSize = sourceFile.length();
        long compressedSize = targetFile.length();

        double compressionRatio = originalSize / (double) compressedSize;

        log.info("Compression ratio {} for {}", compressionRatio, targetFile.getName());

        if (parameters.getOnlyIfMinCompressionRatio().isPresent()) {

            if (compressionRatio < parameters.getOnlyIfMinCompressionRatio().get()) {
                targetFile.delete();
            }

        }
    }
}
