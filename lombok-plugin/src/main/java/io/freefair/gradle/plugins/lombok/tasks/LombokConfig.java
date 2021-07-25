package io.freefair.gradle.plugins.lombok.tasks;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class LombokConfig extends DefaultTask implements LombokTask {

    @Classpath
    private final ConfigurableFileCollection lombokClasspath = getProject().files();

    /**
     * Generates a list containing all the available configuration parameters.
     */
    @Input
    private final Property<Boolean> generate = getProject().getObjects().property(Boolean.class).convention(false);

    /**
     * Displays more information.
     */
    @Input
    private final Property<Boolean> verbose = getProject().getObjects().property(Boolean.class).convention(false);

    /**
     * Also display files that don't mention the key.
     */
    @Input
    private final Property<Boolean> notMentioned = getProject().getObjects().property(Boolean.class).convention(false);

    /**
     * Limit the result to these keys.
     */
    @Input
    @Optional
    private final ListProperty<String> keys = getProject().getObjects().listProperty(String.class);

    /**
     * Paths to java files or directories the configuration is to be printed for.
     */
    @InputFiles
    @PathSensitive(PathSensitivity.ABSOLUTE)
    private final ConfigurableFileCollection paths = getProject().getObjects().fileCollection();

    @OutputFile
    private final RegularFileProperty outputFile = getProject().getObjects().fileProperty();

    public LombokConfig() {
        getOutputs().upToDateWhen(t -> ((LombokConfig) t).getPaths().isEmpty());
    }

    @TaskAction
    public void exec() throws IOException {

        getProject().delete(outputFile);

        List<File> actualPaths = paths.getFiles()
                .stream()
                .filter(File::exists)
                .collect(Collectors.toList());

        if (actualPaths.isEmpty() && !generate.get()) {
            outputFile.get().getAsFile().createNewFile();
            return;
        }

        try (OutputStream out = new FileOutputStream(outputFile.getAsFile().get())) {

            getProject().javaexec(config -> {
                config.setClasspath(getLombokClasspath());
                config.getMainClass().set("lombok.launch.Main");
                config.args("config");

                config.setStandardOutput(out);

                if (generate.getOrElse(false)) {
                    config.args("--generate");
                }

                if (verbose.getOrElse(false)) {
                    config.args("--verbose");
                }

                if (notMentioned.getOrElse(false)) {
                    config.args("--not-mentioned");
                }

                for (String key : keys.getOrElse(Collections.emptyList())) {
                    config.args("--key=" + key.trim());
                }

                for (File path : actualPaths) {
                    config.args(path);
                }
            });
        }
    }
}
