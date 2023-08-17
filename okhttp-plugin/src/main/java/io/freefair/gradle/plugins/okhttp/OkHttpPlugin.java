package io.freefair.gradle.plugins.okhttp;

import io.freefair.gradle.plugins.okhttp.tasks.OkHttpTask;
import okhttp3.logging.HttpLoggingInterceptor;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Lars Grefer
 */
public class OkHttpPlugin implements Plugin<Project> {

    private Project project;
    private OkHttpExtension okHttpExtension;

    @Override
    public void apply(Project project) {
        this.project = project;

        okHttpExtension = project.getExtensions().create("okHttp", OkHttpExtension.class);
        okHttpExtension.getLoggingLevel().convention(project.provider(this::getLevel));

        okHttpExtension.getCacheSize().convention(10 * 1024 * 1024);

        okHttpExtension.getForceCache().convention(
                project.getGradle().getStartParameter().isOffline()
        );

        okHttpExtension.getForceNetwork().convention(
                project.getGradle().getGradle().getStartParameter().isRefreshDependencies()
        );

        project.getTasks().withType(OkHttpTask.class).configureEach(okHttpTask -> {
            okHttpTask.getLoggingLevel().convention(okHttpExtension.getLoggingLevel());
            okHttpTask.getCacheSize().convention(okHttpExtension.getCacheSize());
            okHttpTask.getForceCache().convention(okHttpExtension.getForceCache());
            okHttpTask.getForceNetwork().convention(okHttpExtension.getForceNetwork());
        });
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
