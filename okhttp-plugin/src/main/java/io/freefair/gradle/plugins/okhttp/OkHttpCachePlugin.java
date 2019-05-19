package io.freefair.gradle.plugins.okhttp;

import lombok.Getter;
import okhttp3.Cache;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.initialization.layout.ProjectCacheDir;

import java.io.File;

public class OkHttpCachePlugin implements Plugin<Project> {

    @Getter
    private Cache cache;

    @Override
    public void apply(Project project) {

        if (project != project.getRootProject()) {
            throw new IllegalStateException();
        }

        ProjectCacheDir projectCacheDir = ((ProjectInternal) project).getServices().get(ProjectCacheDir.class);

        cache = new Cache(new File(projectCacheDir.getDir(), getClass().getName()), 50 * 1024 * 1024);

    }
}
