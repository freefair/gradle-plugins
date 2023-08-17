package io.freefair.gradle.plugins.okhttp;

import okhttp3.logging.HttpLoggingInterceptor;
import org.gradle.api.provider.Property;

/**
 * @author Lars Grefer
 */
public abstract class OkHttpExtension {

    public abstract Property<HttpLoggingInterceptor.Level> getLoggingLevel();

    /**
     * Size in bytes for the internal {@link okhttp3.Cache HTTP cache}.
     * Setting this to 0 disables the cache.
     */
    public abstract Property<Integer> getCacheSize();

    public abstract Property<Boolean> getForceCache();

    public abstract Property<Boolean> getForceNetwork();

}
