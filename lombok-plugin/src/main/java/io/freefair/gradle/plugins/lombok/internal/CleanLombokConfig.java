package io.freefair.gradle.plugins.lombok.internal;

import io.freefair.gradle.plugins.lombok.tasks.LombokConfig;
import lombok.SneakyThrows;
import lombok.var;
import org.gradle.api.Action;
import org.gradle.api.NonNullApi;
import org.gradle.api.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lars Grefer
 */
@NonNullApi
class CleanLombokConfig implements Action<Task> {

    @Override
    @SneakyThrows(IOException.class)
    public void execute(Task task) {
        LombokConfig lombokConfig = (LombokConfig) task;

        Path file = lombokConfig.getOutputFile().get().getAsFile().toPath();

        List<String> filtered;
        try (var lines = Files.lines(file)) {
            filtered = lines.filter(line -> line.contains("="))
                    .sorted()
                    .distinct()
                    .collect(Collectors.toList());
        }
        Files.write(file, filtered);
    }
}
