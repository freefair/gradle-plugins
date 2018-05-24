package io.freefair.gradle.plugins.lombok;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;

import java.io.File;
import java.util.stream.Collectors;

@Getter
@Setter
public class Delombok extends JavaExec {

    @Nested
    private DelombokOptions options = new DelombokOptions();

    @InputFiles
    private FileCollection input;

    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "setMain() and args()")
    public Delombok() {
        setMain("lombok.launch.Main");
        args("delombok");
        getArgumentProviders().add(getOptions());
        getArgumentProviders().add(() -> getInput().getFiles().stream()
                .filter(File::isDirectory)
                .map(File::getPath)
                .collect(Collectors.toList()));
    }

    @Override
    public void exec() {
        getProject().delete(getDestinationDir());
        super.exec();
    }

    @OutputDirectory
    public File getDestinationDir() {
        return getOptions().getTarget();
    }

    public void setDestinationDir(File destinationDir) {
        getOptions().setTarget(destinationDir);
    }

}
