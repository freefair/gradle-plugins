package io.freefair.gradle.plugins.lombok.internal;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.gradle.api.file.*;
import org.gradle.api.model.ObjectFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class ConfigFileResolver {

    private final ObjectFactory objectFactory;

    public FileCollection findConfigFiles(SourceDirectorySet sourceSet) {
        ConfigurableFileCollection configFiles = objectFactory.fileCollection();

        for (File sourceDirectory : sourceSet.getSourceDirectories()) {
            configFiles.from(findConfigFiles(sourceDirectory));
        }

        return configFiles;
    }

    public FileCollection findConfigFiles(File dir) {
        ConfigurableFileCollection configFiles = objectFactory.fileCollection();

        configFiles.from(findNestedConfigFiles(dir));

        boolean stopBubbling = false;
        File currentDir = dir.getAbsoluteFile();
        while (!stopBubbling && currentDir != null) {
            File configFile = new File(currentDir, "lombok.config");
            if (configFile.isFile()) {
                configFiles.from(configFile);
                stopBubbling = isStopBubbling(configFile);
            }
            currentDir = currentDir.getParentFile();
        }

        for (File configFile : configFiles) {
            if (containsImport(configFile)) {
                throw new UnsupportedOperationException("imports are used in " + configFile);
            }
        }

        return configFiles;
    }

    private FileTree findNestedConfigFiles(File dir) {
        ConfigurableFileTree subFiles = objectFactory.fileTree();
        subFiles.setDir(dir);
        subFiles.include("**/lombok.config");
        return subFiles;
    }

    private static final Pattern stopBubblingPattern = Pattern.compile("\\s*config\\.stopBubbling\\s*=\\s*true\\s*");
    private static final Pattern importPattern = Pattern.compile("\\s*import\\s+(.+)");

    @SneakyThrows
    private static boolean isStopBubbling(File configFile) {
        try (Stream<String> lines = Files.lines(configFile.toPath())) {
            return lines.anyMatch(line -> stopBubblingPattern.matcher(line).matches());
        }
    }

    @SneakyThrows
    private static boolean containsImport(File configFile) {
        try (Stream<String> lines = Files.lines(configFile.toPath())) {
            return lines.anyMatch(line -> importPattern.matcher(line).matches());
        }
    }
}
