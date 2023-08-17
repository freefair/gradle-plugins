package io.freefair.gradle.plugins.okhttp.tasks;

import io.freefair.gradle.plugins.okhttp.internal.CacheControlInterceptor;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.File;

/**
 * Base class for tasks using an {@link OkHttpClient}.
 *
 * @author Lars Grefer
 */
public abstract class OkHttpTask extends DefaultTask {

    @Console
    public abstract Property<HttpLoggingInterceptor.Level> getLoggingLevel();

    @Internal
    public abstract DirectoryProperty getCacheDir();

    @Input
    @Optional
    public abstract Property<Integer> getCacheSize();

    @Input
    @Optional
    public abstract Property<Boolean> getForceCache();

    @Input
    @Optional
    public abstract Property<Boolean> getForceNetwork();

    @Internal
    private OkHttpClient okHttpClient;

    public OkHttpTask() {
        getCacheSize().convention(10 * 1024 * 1024);
        getCacheDir().fileValue(getTemporaryDir());
        getForceCache().convention(getProject().getGradle().getStartParameter().isOffline());
        getForceNetwork().convention(getProject().getGradle().getStartParameter().isRefreshDependencies());
    }

    protected OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            okHttpClient = buildOkHttpClient();
        }

        return okHttpClient;
    }

    protected OkHttpClient buildOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        File cacheDir = getCacheDir().getAsFile().getOrNull();
        int cacheSize = getCacheSize().getOrElse(0);

        if (cacheDir != null && cacheSize > 1) {
            Cache cache = new Cache(cacheDir, cacheSize);
            builder.cache(cache);
        }

        if (getForceCache().getOrElse(false)) {
            builder.addInterceptor(new CacheControlInterceptor(CacheControl.FORCE_CACHE));
        }
        else if (getForceNetwork().getOrElse(false)) {
            builder.addInterceptor(new CacheControlInterceptor(CacheControl.FORCE_NETWORK));
        }

        if (getLoggingLevel().isPresent()) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(s -> getLogger().lifecycle(s));
            loggingInterceptor.level(getLoggingLevel().get());
            builder.addInterceptor(loggingInterceptor);
        }

        return builder.build();
    }
}
