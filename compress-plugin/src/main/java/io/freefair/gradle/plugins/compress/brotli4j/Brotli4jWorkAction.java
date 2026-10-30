package io.freefair.gradle.plugins.compress.brotli4j;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.encoder.BrotliOutputStream;
import com.aayushatharva.brotli4j.encoder.Encoder;
import io.freefair.gradle.plugins.compress.BrotliWorkParameters;
import org.gradle.workers.WorkAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import static io.freefair.gradle.plugins.compress.tasks.CompressorWorkAction.checkMinCompressionRatio;

public abstract class Brotli4jWorkAction implements WorkAction<BrotliWorkParameters> {
    @Override
    public void execute() {

        Brotli4jLoader.ensureAvailability();

        File inFile = getParameters().getSourceFile().get().getAsFile();
        File outFile = getParameters().getTargetFile().get().getAsFile();

        int quality = getParameters().getQuality().getOrElse(-1);

        if (getParameters().getBest().getOrElse(false)) {
            quality = 11;
        }

        Encoder.Parameters parameters = Encoder.Parameters.create(quality);

        try (BrotliOutputStream outputStream = new BrotliOutputStream(new FileOutputStream(outFile), parameters)) {
            Files.copy(inFile.toPath(), outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        checkMinCompressionRatio(getParameters());
    }
}
