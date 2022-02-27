package io.freefair.gradle.plugins.lombok.tasks.internal;

import lombok.SneakyThrows;
import lombok.launch.LombokApi;
import org.gradle.workers.WorkAction;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * @author Lars Grefer
 */
public abstract class LombokConfigAction implements WorkAction<LombokConfigParameters> {

    @SneakyThrows
    @Override
    public void execute() {
        LombokApi lombokApi = new LombokApi();

        File outputFile = getParameters().getOutputFile().get().getAsFile();

        try (OutputStream out = Files.newOutputStream(outputFile.toPath())) {
            lombokApi.config(out, getParameters().getArgs().get());
        }
    }
}
