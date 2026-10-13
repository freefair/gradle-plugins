package io.freefair.gradle.plugins.compress.tasks;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkParameters;

@SuppressWarnings("UnstableApiUsage")
public interface CompressorWorkParameters extends WorkParameters {

    RegularFileProperty getSourceFile();

    RegularFileProperty getTargetFile();

    /**
     * Minimal Compression Ratio for the file to be created.
     * <p>
     * 1 Means the compressed file must be at least as small as the original file.
     * 2 Means the compressed file must be at least half the size of the original file.
     */
    Property<Double> getOnlyIfMinCompressionRatio();

}
