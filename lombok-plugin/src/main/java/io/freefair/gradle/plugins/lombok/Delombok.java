package io.freefair.gradle.plugins.lombok;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.FileTreeInternal;
import org.gradle.api.internal.file.UnionFileTree;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.util.GUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class Delombok extends SourceTask {

    /**
     * Print the name of each file as it is being delombok-ed.
     */
    @Input
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
    @Input
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
    private final Property<Boolean> print = getProject().getObjects().property(Boolean.class);

    /**
     * Directory to save delomboked files to.
     */
    @OutputDirectory
    private final DirectoryProperty target = newOutputDirectory();

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
    private final Property<Boolean> nocopy = getProject().getObjects().property(Boolean.class);

    @Classpath
    private final ConfigurableFileCollection lombokClasspath = getProject().files();

    @Internal
    private ConfigurableFileCollection input = getProject().files();

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
    public void delombok() {
        getProject().delete(getTarget().getAsFile().get());

        File gradleDependencyDir = new File(getProject().getGradle().getGradleUserHomeDir(), "caches/modules-2/files-2.1");

        getProject().javaexec(delombok -> {
            delombok.setClasspath(getLombokClasspath());
            delombok.setWorkingDir(gradleDependencyDir);
            delombok.setMain("lombok.launch.Main");
            delombok.systemProperty("java.io.tmpdir", getTemporaryDir());
            delombok.args("delombok");

            if (verbose.getOrElse(false)) {
                delombok.args("--verbose");
            }
            getFormat().forEach((key, value) -> {
                String formatValue = key + (GUtil.isTrue(value) ? ":" + value : "");
                delombok.args("--format=" + formatValue);
            });
            if (quiet.getOrElse(false)) {
                delombok.args("--quiet");
            }
            if (getEncoding().isPresent()) {
                delombok.args("--encoding=" + getEncoding().get());
            }

            if (print.getOrElse(false)) {
                delombok.args("--print");
            }

            if (target.isPresent()) {
                delombok.args("--target=" + target.getAsFile().get());
            }

            if (!classpath.isEmpty()) {
                delombok.args("--classpath=" + getClasspath().getAsPath().replace(gradleDependencyDir.getAbsolutePath(), "."));
            }
            if (!sourcepath.isEmpty()) {
                delombok.args("--sourcepath=" + getSourcepath().getAsPath().replace(gradleDependencyDir.getAbsolutePath(), "."));
            }
            if (!bootclasspath.isEmpty()) {
                delombok.args("--bootclasspath=" + getBootclasspath().getAsPath().replace(gradleDependencyDir.getAbsolutePath(), "."));
            }

            if (nocopy.getOrElse(false)) {
                delombok.args("--nocopy");
            }

            delombok.args(input.getFiles().stream()
                    .filter(File::isDirectory)
                    .collect(Collectors.toList())
            );
        });
    }
}
