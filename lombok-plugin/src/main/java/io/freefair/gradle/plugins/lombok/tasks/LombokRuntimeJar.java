package io.freefair.gradle.plugins.lombok.tasks;

import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.NonNullApi;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Console;
import org.gradle.api.tasks.Input;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;

/**
 * Creates a small lombok-runtime.jar with the runtime
 * dependencies of all lombok transformations that have them,
 * and prints the names of each lombok transformation that
 * requires the lombok-runtime.jar at runtime.
 *
 * @author Lars Grefer
 */
@Getter
@NonNullApi
@CacheableTask
public class LombokRuntimeJar extends LombokJarTask {

    @Getter(AccessLevel.NONE)
    private final ExecOperations execOperations;

    /**
     * Prints those lombok transformations that require lombok-runtime.jar.
     */
    @Console
    private final Property<Boolean> print = getProject().getObjects().property(Boolean.class).convention(false);

    /**
     * Creates the lombok-runtime.jar.
     */
    @Input
    private final Property<Boolean> create = getProject().getObjects().property(Boolean.class).convention(true);

    @Inject
    public LombokRuntimeJar(ExecOperations execOperations) {
        this.execOperations = execOperations;
        getArchiveAppendix().convention("runtime");
    }

    @Override
    public void copy() {
        execOperations.javaexec(runtimeJar -> {
            runtimeJar.setClasspath(getLombokClasspath());
            runtimeJar.getMainClass().set("lombok.launch.Main");
            runtimeJar.args("createRuntime");

            if (print.get()) {
                runtimeJar.args("--print");
            }

            if (create.get()) {
                runtimeJar.args("--create");
                runtimeJar.args("--output=" + getArchiveFile().get().getAsFile().getAbsolutePath());
            }
        });
    }
}
