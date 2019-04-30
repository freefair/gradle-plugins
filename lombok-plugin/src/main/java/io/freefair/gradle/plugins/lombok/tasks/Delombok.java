package io.freefair.gradle.plugins.lombok.tasks;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.FileTreeInternal;
import org.gradle.api.internal.file.UnionFileTree;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.util.GUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class Delombok extends DefaultTask implements LombokTask {

    /**
     * Print the name of each file as it is being delombok-ed.
     */
    @Console
    private final Property<Boolean> verbose = getProject().getObjects().property(Boolean.class);

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
    private final Property<Boolean> quiet = getProject().getObjects().property(Boolean.class);

    /**
     * Sets the encoding of your source files.
     * Defaults to the system default charset.
     * Example: "UTF-8"
     */
    @Input
    @Optional
    private final Property<String> encoding = getProject().getObjects().property(String.class);

    /**
     * Print delombok-ed code to standard output instead of saving it in target directory.
     */
    @Input
    @Optional
    private final Property<Boolean> print = getProject().getObjects().property(Boolean.class);

    /**
     * Directory to save delomboked files to.
     */
    @OutputDirectory
    private final DirectoryProperty target = getProject().getObjects().directoryProperty();

    /**
     * Classpath (analogous to javac -cp option).
     */
    @Classpath
    @Optional
    private final ConfigurableFileCollection classpath = getProject().files();

    /**
     * Sourcepath (analogous to javac -sourcepath option).
     */
    @InputFiles
    @Optional
    private final ConfigurableFileCollection sourcepath = getProject().files();

    /**
     * override Bootclasspath (analogous to javac -bootclasspath option)
     */
    @Classpath
    @Optional
    private final ConfigurableFileCollection bootclasspath = getProject().files();

    /**
     * Lombok will only delombok source files.
     * Without this option, non-java, non-class files are copied to the target directory.
     */
    @Input
    @Optional
    private final Property<Boolean> nocopy = getProject().getObjects().property(Boolean.class);

    @Classpath
    private final ConfigurableFileCollection lombokClasspath = getProject().files();

    @Internal
    private final ConfigurableFileCollection input = getProject().files();

    @InputFiles
    @SkipWhenEmpty
    protected FileTree getFilteredInput() {
        List<FileTreeInternal> collect = input.getFiles().stream()
                .filter(File::isDirectory)
                .map(dir -> getProject().fileTree(dir))
                .map(FileTreeInternal.class::cast)
                .collect(Collectors.toList());

        return new UnionFileTree("actual " + getName() + " input", collect);
    }

    @TaskAction
    public void delombok() throws IOException {
        getProject().delete(getTarget().getAsFile().get());

        List<String> args = new LinkedList<>();

        if (verbose.getOrElse(false)) {
            args.add("--verbose");
        }
        getFormat().forEach((key, value) -> {
            String formatValue = key + (GUtil.isTrue(value) ? ":" + value : "");
            args.add("--format=" + formatValue);
        });
        if (quiet.getOrElse(false)) {
            args.add("--quiet");
        }
        if (getEncoding().isPresent()) {
            args.add("--encoding=" + getEncoding().get());
        }

        if (print.getOrElse(false)) {
            args.add("--print");
        }

        if (target.isPresent()) {
            args.add("--target=" + escape(getTarget().getAsFile().get().getAbsolutePath()));
        }

        if (!classpath.isEmpty()) {
            args.add("--classpath=" + escape(getClasspath().getAsPath()));
        }
        if (!sourcepath.isEmpty()) {
            args.add("--sourcepath=" + escape(getSourcepath().getAsPath()));
        }
        if (!bootclasspath.isEmpty()) {
            args.add("--bootclasspath=" + escape(getBootclasspath().getAsPath()));
        }

        if (nocopy.getOrElse(false)) {
            args.add("--nocopy");
        }

        File optionsFile = new File(getTemporaryDir(), "delombok.options");

        Files.write(optionsFile.toPath(), args);

        getProject().javaexec(delombok -> {
            delombok.setClasspath(getLombokClasspath());
            delombok.setMain("lombok.launch.Main");
            delombok.args("delombok");

            delombok.args("@" + optionsFile);

            delombok.args(input.getFiles().stream()
                    .filter(File::isDirectory)
                    .collect(Collectors.toList())
            );
        });
    }

    private static String escape(String path) {
        return path.replace("\\", "\\\\");
    }
}
