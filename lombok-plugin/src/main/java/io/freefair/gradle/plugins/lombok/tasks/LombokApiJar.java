package io.freefair.gradle.plugins.lombok.tasks;

import org.gradle.api.NonNullApi;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.internal.ProcessOperations;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;

/**
 * Creates a small lombok-api.jar with the annotations and other public API
 * classes of all lombok features. This is primarily useful to include in your
 * android projects.
 *
 * @author Lars Grefer
 */
@NonNullApi
@CacheableTask
public abstract class LombokApiJar extends LombokJarTask {

    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();
    @Inject
    protected abstract ProcessOperations getProcessOperations();

    public LombokApiJar() {
        getArchiveAppendix().convention("api");
    }

    @TaskAction
    public void copy() {
        getFileSystemOperations().delete(spec -> spec.delete(getArchiveFile()).setFollowSymlinks(false));

        File destinationDir = getDestinationDirectory().getAsFile().get();
        getProcessOperations().javaexec(apiJar -> {
            if (getLauncher().isPresent()) {
                apiJar.setExecutable(getLauncher().get().getExecutablePath().getAsFile().getAbsolutePath());
            }
            apiJar.setClasspath(getLombokClasspath());
            apiJar.getMainClass().set("lombok.launch.Main");
            apiJar.args("publicApi", destinationDir.getAbsolutePath());
        });

        new File(destinationDir, "lombok-api.jar")
                .renameTo(getArchiveFile().get().getAsFile());
    }
}
