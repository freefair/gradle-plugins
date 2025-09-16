package io.freefair.gradle.plugins.lombok;

import org.gradle.api.provider.Property;

/**
 * @author Lars Grefer
 * @see LombokPlugin
 */
public abstract class LombokExtension {

    public static final String LOMBOK_VERSION = "1.18.40";

    /**
     * The version of Lombok which will be used.
     */
    public abstract Property<String> getVersion();

    public abstract Property<Boolean> getDisableConfig();

    public LombokExtension() {
        getVersion().convention(LOMBOK_VERSION);

        getDisableConfig().convention(System.getProperty("lombok.disableConfig") != null);
    }
}
