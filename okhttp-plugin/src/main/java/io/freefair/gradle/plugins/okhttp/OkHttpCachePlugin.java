package io.freefair.gradle.plugins.okhttp;

import lombok.Getter;
import okhttp3.Cache;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.initialization.layout.ProjectCacheDir;

import java.io.File;

/**
 * @author Lars Grefer
 */
@Deprecated
public class OkHttpCachePlugin implements Plugin<Project> {

    @Getter
    private Cache cache;
    private OkHttpCacheExtension extension;

    @Override
    public void apply(Project project) {

        if (project != project.getRootProject()) {
            throw new IllegalStateException();
        }

        extension = project.getExtensions().create("okHttpCache", OkHttpCacheExtension.class);

        ProjectCacheDir projectCacheDir = ((ProjectInternal) project).getServices().get(ProjectCacheDir.class);

        File directory = new File(projectCacheDir.getDir(), getClass().getName());
        extension.getDirectory().set(directory);
        extension.getMaxSize().set(50L * 1024 * 1024);
    }

    public synchronized Cache getCache() {
        if (cache == null) {
            cache = new Cache(extension.getDirectory().get().getAsFile(), extension.getMaxSize().get());
        }
        return cache;
    }
}
