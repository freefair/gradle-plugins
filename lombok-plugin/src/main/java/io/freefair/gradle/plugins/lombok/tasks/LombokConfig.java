package io.freefair.gradle.plugins.lombok.tasks;

import io.freefair.gradle.plugins.lombok.internal.ConfigUtil;
import io.freefair.gradle.plugins.lombok.tasks.internal.LombokConfigAction;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.*;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkerExecutor;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Prints the configurations for the provided paths to standard out.
 *
 * @author Lars Grefer
 */
@Getter
@Setter
@CacheableTask
public abstract class LombokConfig extends DefaultTask implements LombokTask {

    @Inject
    protected abstract WorkerExecutor getWorkerExecutor();

    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();

    @Inject
    protected abstract ExecOperations getExecOperations();

    @Nested
    @Optional
    public abstract Property<JavaLauncher> getLauncher();

    @Classpath
    public abstract ConfigurableFileCollection getLombokClasspath();

    /**
     * Generates a list containing all the available configuration parameters.
     */
    @Input
    public abstract Property<Boolean> getGenerate();

    /**
     * Displays more information.
     */
    @Input
    public abstract Property<Boolean> getVerbose();

    /**
     * Also display files that don't mention the key.
     */
    @Input
    public abstract Property<Boolean> getNotMentioned();

    /**
     * Limit the result to these keys.
     */
    @Input
    @Optional
    public abstract ListProperty<String> getKeys();

    /**
     * Paths to java files or directories the configuration is to be printed for.
     */
    @Internal
    public abstract ConfigurableFileCollection getPaths();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @Input
    @Optional
    public abstract Property<Boolean> getFork();

    public LombokConfig() {
        getGenerate().convention(false);
        getVerbose().convention(false);
        getNotMentioned().convention(false);
        getOutputs().upToDateWhen(t -> ((LombokConfig) t).getConfigFiles() != null);
        getOutputs().doNotCacheIf("Config Imports were used", t -> ((LombokConfig) t).getConfigFiles() == null);
    }

    @Input
    protected List<String> getInputPaths() {
        return getPaths().getFiles()
                .stream()
                .map(File::getPath)
                .collect(Collectors.toList());
    }

    @InputFiles
    @Optional
    @Nullable
    @PathSensitive(PathSensitivity.RELATIVE)
    @SneakyThrows
    protected Set<File> getConfigFiles() {
        if (getPaths().isEmpty()) {
            return Collections.emptySet();
        }

        Set<File> configFiles = new HashSet<>();

        for (File path : getPaths()) {
            Set<File> filesForPath = ConfigUtil.resolveConfigFilesForPath(path);
            if (filesForPath == null) {
                //Imports Used
                return null;
            }
            configFiles.addAll(filesForPath);
        }

        return configFiles;
    }

    @TaskAction
    public void exec() throws IOException {
        getFileSystemOperations().delete(spec -> spec.delete(getOutputFile()).setFollowSymlinks(false));

        List<File> actualPaths = getPaths().getFiles()
                .stream()
                .filter(File::exists)
                .collect(Collectors.toList());

        if (actualPaths.isEmpty() && !getGenerate().get()) {
            getOutputFile().get().getAsFile().createNewFile();
            return;
        }

        List<String> args = new LinkedList<>();

        if (getGenerate().getOrElse(false)) {
            args.add("--generate");
        }

        if (getVerbose().getOrElse(false)) {
            args.add("--verbose");
        }

        if (getNotMentioned().getOrElse(false)) {
            args.add("--not-mentioned");
        }

        for (String key : getKeys().getOrElse(Collections.emptyList())) {
            args.add("--key=" + key.trim());
        }

        for (File path : actualPaths) {
            args.add(path.getAbsolutePath());
        }

        if (getFork().getOrElse(false)) {
            try (OutputStream out = Files.newOutputStream(getOutputFile().getAsFile().get().toPath())) {

                long start = System.nanoTime();
                getExecOperations().javaexec(config -> {
                    if (getLauncher().isPresent()) {
                        config.setExecutable(getLauncher().get().getExecutablePath().getAsFile().getAbsolutePath());
                    }
                    config.setClasspath(getLombokClasspath());
                    config.setMaxHeapSize("16M");
                    config.getMainClass().set("lombok.launch.Main");
                    config.args("config");

                    config.setStandardOutput(out);

                    config.args(args);
                });

                Duration duration = Duration.ofNanos(System.nanoTime() - start);
                if (duration.getSeconds() > 1) {
                    getLogger().warn("lombok config {} took {}ms", args, duration.toMillis());
                }
                else {
                    getLogger().info("lombok config {} took {}ms", args, duration.toMillis());
                }
            }
        }
        else {
            getWorkerExecutor()
                    .classLoaderIsolation(cl -> cl.getClasspath().from(getLombokClasspath()))
                    .submit(LombokConfigAction.class, params -> {
                        params.getArgs().set(args);
                        params.getOutputFile().set(getOutputFile());
                    });
        }
    }
}
