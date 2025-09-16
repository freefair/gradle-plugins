package io.freefair.gradle.plugins.lombok.internal;

import io.freefair.gradle.plugins.lombok.tasks.LombokConfig;
import lombok.SneakyThrows;
import org.gradle.api.Action;
import org.gradle.api.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lars Grefer
 */
public class CleanLombokConfig implements Action<Task> {

    @Override
    @SneakyThrows(IOException.class)
    public void execute(Task task) {
        LombokConfig lombokConfig = (LombokConfig) task;

        Path file = lombokConfig.getOutputFile().get().getAsFile().toPath();

        List<String> filtered;
        try (Stream<String> lines = Files.lines(file)) {
            filtered = lines.filter(line -> line.contains("="))
                    .sorted()
                    .distinct()
                    .collect(Collectors.toList());
        }
        Files.write(file, filtered);
    }
}
