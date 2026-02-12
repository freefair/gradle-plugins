package io.freefair.gradle.plugins.lombok;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;

/**
 * @author Lars Grefer
 * @see LombokPlugin
 */
public abstract class LombokExtension {

    public static final String LOMBOK_VERSION = "1.18.42";

    /**
     * The version of Lombok which will be used.
     */
    public abstract Property<String> getVersion();

    public abstract Property<Boolean> getDisableConfig();

    @Inject
    protected abstract ProviderFactory getProviders();

    public LombokExtension() {
        getVersion().convention(LOMBOK_VERSION);

        getDisableConfig().convention(
            getProviders().systemProperty("lombok.disableConfig")
                .map(v -> true)
                .orElse(false)
        );
    }
}
