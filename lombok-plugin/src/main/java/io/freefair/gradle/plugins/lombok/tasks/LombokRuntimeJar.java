package io.freefair.gradle.plugins.lombok.tasks;

import lombok.Getter;
import org.gradle.api.NonNullApi;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Console;
import org.gradle.api.tasks.Input;

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

    public LombokRuntimeJar() {
        getArchiveAppendix().convention("runtime");
    }

    @Override
    public void copy() {
        getProject().javaexec(runtimeJar -> {
            runtimeJar.setClasspath(getLombokClasspath());
            runtimeJar.setMain("lombok.launch.Main");
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
