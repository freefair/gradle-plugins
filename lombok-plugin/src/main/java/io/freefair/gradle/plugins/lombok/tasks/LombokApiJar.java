package io.freefair.gradle.plugins.lombok.tasks;

import lombok.AccessLevel;
import lombok.Getter;
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
@Getter
@NonNullApi
@CacheableTask
public class LombokApiJar extends LombokJarTask {

    @Getter(AccessLevel.NONE)
    private final FileSystemOperations fileSystemOperations;
    @Getter(AccessLevel.NONE)
    private final ProcessOperations processOperations;

    @Inject
    public LombokApiJar(FileSystemOperations fileSystemOperations, ProcessOperations processOperations) {
        this.fileSystemOperations = fileSystemOperations;
        this.processOperations = processOperations;
        getArchiveAppendix().convention("api");
    }

    @TaskAction
    public void copy() {
        fileSystemOperations.delete(spec -> spec.delete(getArchiveFile()).setFollowSymlinks(false));

        File destinationDir = getDestinationDirectory().getAsFile().get();
        processOperations.javaexec(apiJar -> {
            apiJar.setClasspath(getLombokClasspath());
            apiJar.getMainClass().set("lombok.launch.Main");
            apiJar.args("publicApi", destinationDir.getAbsolutePath());
        });

        new File(destinationDir, "lombok-api.jar")
                .renameTo(getArchiveFile().get().getAsFile());
    }
}
