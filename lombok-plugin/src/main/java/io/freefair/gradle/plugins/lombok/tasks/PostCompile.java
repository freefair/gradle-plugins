package io.freefair.gradle.plugins.lombok.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;

/**
 * Runs registered post compiler handlers to against existing class files, modifying them in the process.
 *
 * @author Lars Grefer
 */
@CacheableTask
public abstract class PostCompile extends DefaultTask implements LombokTask {

    @Inject
    protected abstract ExecOperations getExecOperations();

    @Nested
    @Optional
    public abstract Property<JavaLauncher> getLauncher();

    @Classpath
    public abstract ConfigurableFileCollection getLombokClasspath();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    @SkipWhenEmpty
    public abstract ConfigurableFileCollection getClassFiles();

    @Console
    public abstract Property<Boolean> getVerbose();

    public PostCompile() {
        getVerbose().convention(false);
    }

    @TaskAction
    public void postCompile() {
        getExecOperations().javaexec(postCompile -> {
            if (getLauncher().isPresent()) {
                postCompile.setExecutable(getLauncher().get().getExecutablePath().getAsFile().getAbsolutePath());
            }
            postCompile.setClasspath(getLombokClasspath());
            postCompile.getMainClass().set("lombok.launch.Main");
            postCompile.args("post-compile");

            if (getVerbose().get()) {
                postCompile.args("--verbose");
            }

            getClassFiles().forEach(file -> postCompile.args(file.getAbsolutePath()));
        });
    }
}
