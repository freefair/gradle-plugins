package io.freefair.gradle.plugins.compress.tasks;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.workers.WorkParameters;

@SuppressWarnings("UnstableApiUsage")
public interface CompressorWorkParameters extends WorkParameters {

    RegularFileProperty getSourceFile();

    RegularFileProperty getTargetFile();

}
