package io.freefair.gradle.plugins.okhttp.tasks;

import io.freefair.gradle.plugins.okhttp.internal.CacheControlInterceptor;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Console;
import org.gradle.api.tasks.Internal;

public abstract class OkHttpTask extends DefaultTask {

    @Console
    public abstract Property<HttpLoggingInterceptor.Level> getLoggingLevel();

    @Internal
    private OkHttpClient okHttpClient;

    protected OkHttpClient getOkHttpClient() {
        if(okHttpClient == null) {
            okHttpClient = buildOkHttpClient();
        }

        return okHttpClient;
    }

    protected OkHttpClient buildOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder = builder.cache(new Cache(getTemporaryDir(), 10 * 1024 * 1024));

        if (getProject().getGradle().getStartParameter().isOffline()) {
            builder = builder.addInterceptor(new CacheControlInterceptor(CacheControl.FORCE_CACHE));
        }
        else if (getProject().getGradle().getStartParameter().isRefreshDependencies()) {
            builder = builder.addInterceptor(new CacheControlInterceptor(CacheControl.FORCE_NETWORK));
        }

        if (getLoggingLevel().isPresent()) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(s -> getLogger().lifecycle(s));
            loggingInterceptor.level(getLoggingLevel().get());
            builder = builder.addInterceptor(loggingInterceptor);
        }

        return builder.build();

    }
}
