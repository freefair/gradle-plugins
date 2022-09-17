package io.freefair.gradle.plugins.lombok.tasks;

import org.gradle.api.NonNullApi;
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
@NonNullApi
@CacheableTask
public abstract class LombokRuntimeJar extends LombokJarTask {

    @Inject
    protected abstract ExecOperations getExecOperations();

    /**
     * Prints those lombok transformations that require lombok-runtime.jar.
     */
    @Console
    public abstract Property<Boolean> getPrint();

    /**
     * Creates the lombok-runtime.jar.
     */
    @Input
    public abstract Property<Boolean> getCreate();

    public LombokRuntimeJar() {
        getArchiveAppendix().convention("runtime");
        getPrint().convention(false);
        getCreate().convention(true);
    }

    @Override
    public void copy() {
        getExecOperations().javaexec(runtimeJar -> {
            if (getLauncher().isPresent()) {
                runtimeJar.setExecutable(getLauncher().get().getExecutablePath().getAsFile().getAbsolutePath());
            }
            runtimeJar.setClasspath(getLombokClasspath());
            runtimeJar.getMainClass().set("lombok.launch.Main");
            runtimeJar.args("createRuntime");

            if (getPrint().get()) {
                runtimeJar.args("--print");
            }

            if (getCreate().get()) {
                runtimeJar.args("--create");
                runtimeJar.args("--output=" + getArchiveFile().get().getAsFile().getAbsolutePath());
            }
        });
    }
}
