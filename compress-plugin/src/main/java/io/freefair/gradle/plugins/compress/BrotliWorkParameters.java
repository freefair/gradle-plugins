package io.freefair.gradle.plugins.compress;

import io.freefair.gradle.plugins.compress.tasks.CompressorWorkParameters;
import org.gradle.api.provider.Property;

public interface BrotliWorkParameters extends CompressorWorkParameters {

    Property<Integer> getQuality();

    /**
     * force output file overwrite
     */
    Property<Boolean> getForce();

    /**
     * use best compression level (11) (default)
     */
    Property<Boolean> getBest();
}
