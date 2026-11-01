package io.freefair.gradle.plugins.compress.brotli4j;

import io.freefair.gradle.plugins.compress.DefaultVersions;
import org.gradle.api.provider.Property;

public abstract class Brotli4jExtension {

    public abstract Property<String> getVersion();

    public Brotli4jExtension() {
        getVersion().convention(DefaultVersions.BROTLI4J_VERSION);
    }
}
