package io.freefair.gradle.plugins.okhttp;

import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.annotation.Nullable;

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
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(project.getLogger()::info);
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

            okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request request = chain.request();

                        if (project.getGradle().getStartParameter().isOffline()) {
                            request = request.newBuilder()
                                    .cacheControl(CacheControl.FORCE_CACHE)
                                    .build();
                        }

                        return chain.proceed(request);
                    })
                    .addInterceptor(loggingInterceptor)
                    .cache(okHttpCachePlugin.getCache())
                    .build();
        }
        return okHttpClient;
    }
}
