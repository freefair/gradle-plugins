package io.freefair.gradle.plugins.lombok.tasks;

import lombok.Getter;
import org.gradle.api.NonNullApi;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.TaskAction;

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

    public LombokApiJar() {
        getArchiveAppendix().convention("api");
    }

    @TaskAction
    public void copy() {
        getProject().delete(getArchiveFile());

        File destinationDir = getDestinationDirectory().getAsFile().get();
        getProject().javaexec(apiJar -> {
            apiJar.setClasspath(getLombokClasspath());
            apiJar.setMain("lombok.launch.Main");
            apiJar.args("publicApi", destinationDir.getAbsolutePath());
        });

        new File(destinationDir, "lombok-api.jar")
                .renameTo(getArchiveFile().get().getAsFile());
    }
}
