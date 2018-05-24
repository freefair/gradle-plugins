package io.freefair.gradle.plugins.lombok;

import lombok.Data;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.Optional;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradle.util.CollectionUtils;
import org.gradle.util.GUtil;

import java.io.File;
import java.util.*;

@Data
public class DelombokOptions implements CommandLineArgumentProvider {

    /**
     * Print the name of each file as it is being delombok-ed.
     */
    @Input
    private boolean verbose = false;

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
    private boolean quiet = false;

    /**
     * Sets the encoding of your source files.
     * Defaults to the system default charset.
     * Example: "UTF-8"
     */
    @Input
    @Optional
    private String encoding;

    /**
     * Print delombok-ed code to standard output instead of saving it in target directory.
     */
    @Input
    private boolean print = false;

    /**
     * Directory to save delomboked files to.
     */
    @OutputDirectory
    private File target;

    /**
     * Classpath (analogous to javac -cp option).
     */
    @Classpath
    @Optional
    private FileCollection classpath;

    /**
     * Sourcepath (analogous to javac -sourcepath option).
     */
    @InputFiles
    @Optional
    private FileCollection sourcepath;

    /**
     * override Bootclasspath (analogous to javac -bootclasspath option)
     */
    @Classpath
    @Optional
    private FileCollection bootclasspath;

    /**
     * Lombok will only delombok source files.
     * Without this option, non-java, non-class files are copied to the target directory.
     */
    @Input
    private boolean nocopy = false;

    @Override
    public Iterable<String> asArguments() {
        List<String> args = new ArrayList<>();

        if (isVerbose()) {
            args.add("--verbose");
        }
        getFormat().forEach((key, value) -> {
            String formatValue = key + (GUtil.isTrue(value) ? ":" + value : "");
            args.add("--format=" + formatValue);
        });
        if (isQuiet()) {
            args.add("--quiet");
        }
        if (GUtil.isTrue(getEncoding())) {
            args.add("--encoding=" + getEncoding());
        }

        if (isPrint()) {
            args.add("--print");
        }

        if (getTarget() != null) {
            args.add("--target=" + getTarget());
        }

        if (GUtil.isTrue(getClasspath())) {
            args.add("--classpath=" + CollectionUtils.join(File.pathSeparator, getClasspath().getFiles()));
        }
        if (GUtil.isTrue(getSourcepath())) {
            args.add("--sourcepath=" + CollectionUtils.join(File.pathSeparator, getSourcepath().getFiles()));
        }
        if (GUtil.isTrue(getBootclasspath())) {
            args.add("--bootclasspath=" + CollectionUtils.join(File.pathSeparator, getBootclasspath().getFiles()));
        }

        if (isNocopy()) {
            args.add("--nocopy");
        }

        return args;
    }
}
