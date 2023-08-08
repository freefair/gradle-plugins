package io.freefair.gradle.plugins.lombok.tasks.internal;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.launch.LombokApi;
import org.gradle.workers.WorkAction;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;

/**
 * @author Lars Grefer
 */
@Slf4j
public abstract class LombokConfigAction implements WorkAction<LombokConfigParameters> {

    @SneakyThrows
    @Override
    public void execute() {
        LombokApi lombokApi = new LombokApi();

        File outputFile = getParameters().getOutputFile().get().getAsFile();

        long start = System.nanoTime();

        List<String> arguments = getParameters().getArgs().get();
        try (OutputStream out = Files.newOutputStream(outputFile.toPath())) {
            lombokApi.config(out, arguments);
        }

        Duration duration = Duration.ofNanos(System.nanoTime() - start);
        if (duration.getSeconds() > 1) {
            log.warn("lombok config {} took {}ms", arguments, duration.toMillis());
        }
        else {
            log.info("lombok config {} took {}ms", arguments, duration.toMillis());
        }
    }
}
