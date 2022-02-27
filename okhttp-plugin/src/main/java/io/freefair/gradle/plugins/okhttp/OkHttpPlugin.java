package io.freefair.gradle.plugins.okhttp;

import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * @author Lars Grefer
 */
public class OkHttpPlugin implements Plugin<Project> {

    @Nullable
    private OkHttpClient okHttpClient;
    private OkHttpCachePlugin okHttpCachePlugin;
    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;

        okHttpCachePlugin = project.getRootProject().getPlugins().apply(OkHttpCachePlugin.class);
    }

    public OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .cache(okHttpCachePlugin.getCache());

            if (project.getGradle().getStartParameter().isOffline()) {
                builder = builder.addInterceptor(new ForceCacheInterceptor());
            }

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(project.getLogger()::info);
            loggingInterceptor.level(HttpLoggingInterceptor.Level.BASIC);
            builder = builder.addInterceptor(loggingInterceptor);

            okHttpClient = builder.build();
        }
        return okHttpClient;
    }

    private static class ForceCacheInterceptor implements Interceptor {
        @Nonnull
        @Override
        public Response intercept(@Nonnull Chain chain) throws IOException {
            Request newRequest = chain.request()
                    .newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();

            return chain.proceed(newRequest);
        }
    }
}
