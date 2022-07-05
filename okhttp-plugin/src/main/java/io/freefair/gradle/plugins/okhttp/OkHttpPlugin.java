package io.freefair.gradle.plugins.okhttp;

import io.freefair.gradle.plugins.okhttp.internal.CacheControlInterceptor;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
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
    private OkHttpExtension okHttpExtension;

    @Override
    public void apply(Project project) {
        this.project = project;

        okHttpCachePlugin = project.getRootProject().getPlugins().apply(OkHttpCachePlugin.class);

        okHttpExtension = project.getExtensions().create("okHttp", OkHttpExtension.class);
        okHttpExtension.getLoggingLevel().convention(project.provider(this::getLevel));

    }

    public OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .cache(okHttpCachePlugin.getCache());

            if (project.getGradle().getStartParameter().isOffline()) {
                builder = builder.addInterceptor(new CacheControlInterceptor(CacheControl.FORCE_CACHE));
            }
            else if (project.getGradle().getStartParameter().isRefreshDependencies()) {
                builder = builder.addInterceptor(new CacheControlInterceptor(CacheControl.FORCE_NETWORK));
            }

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(project.getLogger()::info);
            loggingInterceptor.level(okHttpExtension.getLoggingLevel().get());
            builder = builder.addInterceptor(loggingInterceptor);

            okHttpClient = builder.build();
        }
        return okHttpClient;
    }

    private HttpLoggingInterceptor.Level getLevel() {
        if (project.getLogger().isTraceEnabled()) {
            return HttpLoggingInterceptor.Level.BODY;
        }
        else if (project.getLogger().isDebugEnabled()) {
            return HttpLoggingInterceptor.Level.HEADERS;
        }
        else if (project.getLogger().isInfoEnabled()) {
            return HttpLoggingInterceptor.Level.BASIC;
        }

        return HttpLoggingInterceptor.Level.NONE;
    }

}
