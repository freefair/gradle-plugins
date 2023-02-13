package io.freefair.gradle.plugins.lombok.tasks;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.*;
import org.gradle.api.internal.file.FileTreeInternal;
import org.gradle.api.internal.file.UnionFileTree;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.*;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Applies lombok transformations without compiling your
 * java code (so, 'unpacks' lombok annotations and such).
 *
 * @author Lars Grefer
 */
@Getter
@Setter
@CacheableTask
public abstract class Delombok extends DefaultTask implements LombokTask {

    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();
    @Inject
    protected abstract ExecOperations getExecOperations();

    @Nested
    @Optional
    public abstract Property<JavaLauncher> getLauncher();

    /**
     * Print the name of each file as it is being delombok-ed.
     */
    @Console
    public abstract Property<Boolean> getVerbose();

    /**
     * Sets formatting rules.
     * Use --format-help to list all available rules.
     * Unset format rules are inferred by scanning the source for usages.
     */
    @Input
    private Map<String, String> format = new HashMap<>();

    /**
     * No warnings or errors will be emitted to standard error.
     */
    @Console
    public abstract Property<Boolean> getQuiet();

    /**
     * Sets the encoding of your source files.
     * Defaults to the system default charset.
     * Example: "UTF-8"
     */
    @Input
    @Optional
    public abstract Property<String> getEncoding();

    /**
     * Print delombok-ed code to standard output instead of saving it in target directory.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getPrint();

    /**
     * Directory to save delomboked files to.
     */
    @OutputDirectory
    public abstract DirectoryProperty getTarget();

    /**
     * Classpath (analogous to javac -cp option).
     */
    @Classpath
    @Optional
    public abstract ConfigurableFileCollection getClasspath();

    /**
     * Sourcepath (analogous to javac -sourcepath option).
     */
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    @Optional
    public abstract ConfigurableFileCollection getSourcepath();

    /**
     * override Bootclasspath (analogous to javac -bootclasspath option)
     */
    @Classpath
    @Optional
    public abstract ConfigurableFileCollection getBootclasspath();

    /**
     * Module path (analogous to javac --module-path option)
     */
    @Classpath
    @Optional
    public abstract ConfigurableFileCollection getModulePath();

    /**
     * Lombok will only delombok source files.
     * Without this option, non-java, non-class files are copied to the target directory.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getNocopy();

    @Classpath
    public abstract ConfigurableFileCollection getLombokClasspath();

    @Internal
    public abstract ConfigurableFileCollection getInput();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    @SkipWhenEmpty
    @IgnoreEmptyDirectories
    protected FileTree getFilteredInput() {
        ConfigurableFileTree fileTree = null;

        for (File file : getInput().getFiles()) {
            if (file.isDirectory()) {
                if (fileTree == null) {
                    fileTree = getProject().fileTree(file);
                }
                else {
                    fileTree.from(file);
                }
            }
        }

        return fileTree;
    }

    @TaskAction
    public void delombok() throws IOException {
        getFileSystemOperations().delete(spec -> spec.delete(getTarget()).setFollowSymlinks(false));

        List<String> args = new LinkedList<>();

        if (getVerbose().getOrElse(false)) {
            args.add("--verbose");
        }
        getFormat().forEach((key, value) -> {
            String formatValue = key + (value != null && !value.isEmpty() ? ":" + value : "");
            args.add("--format=" + formatValue);
        });
        if (getQuiet().getOrElse(false)) {
            args.add("--quiet");
        }
        if (getEncoding().isPresent()) {
            args.add("--encoding=" + getEncoding().get());
        }

        if (getPrint().getOrElse(false)) {
            args.add("--print");
        }

        if (getTarget().isPresent()) {
            args.add("--target=" + escape(getTarget().getAsFile().get().getAbsolutePath()));
        }

        if (!getClasspath().isEmpty()) {
            args.add("--classpath=" + escape(getClasspath().getAsPath()));
        }
        if (!getSourcepath().isEmpty()) {
            args.add("--sourcepath=" + escape(getSourcepath().getAsPath()));
        }
        if (!getBootclasspath().isEmpty()) {
            args.add("--bootclasspath=" + escape(getBootclasspath().getAsPath()));
        }

        if (!getModulePath().isEmpty()) {
            args.add("--module-path=" + escape(getModulePath().getAsPath()));
        }

        if (getNocopy().getOrElse(false)) {
            args.add("--nocopy");
        }

        File optionsFile = new File(getTemporaryDir(), "delombok.options");

        Files.write(optionsFile.toPath(), args);

        getExecOperations().javaexec(delombok -> {
            if (getLauncher().isPresent()) {
                delombok.setExecutable(getLauncher().get().getExecutablePath().getAsFile().getAbsolutePath());
            }
            delombok.setClasspath(getLombokClasspath());
            delombok.getMainClass().set("lombok.launch.Main");
            delombok.args("delombok");

            delombok.args("@" + optionsFile);

            delombok.args(getInput().getFiles().stream()
                    .filter(File::isDirectory)
                    .collect(Collectors.toList())
            );
        });
    }

    private static String escape(String path) {
        return path.replace("\\", "\\\\")
                .replace(" ", "\\ ");
    }
}
