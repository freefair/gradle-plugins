package io.freefair.gradle.plugins.compress.brotli4j;

import org.gradle.api.provider.Property;

public abstract class Brotli4jExtension {

    public abstract Property<String> getVersion();

    public Brotli4jExtension() {
        getVersion().convention("1.24.0");
    }
}
