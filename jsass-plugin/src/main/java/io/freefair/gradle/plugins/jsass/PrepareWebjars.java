package io.freefair.gradle.plugins.jsass;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

@Getter
@Setter
public class PrepareWebjars extends DefaultTask {

    @OutputDirectory
    private final DirectoryProperty outputDirectory= newOutputDirectory();

    @InputFiles
    private final ConfigurableFileCollection webjars = getProject().files();

    @TaskAction
    public void extractWebjars() {
        getProject().sync(sync -> {
            sync.into(outputDirectory);

            sync.setDuplicatesStrategy(DuplicatesStrategy.WARN);

            webjars.filter(File::isFile).getFiles().forEach(file ->
                    sync.from(getProject().zipTree(file), jarSpec -> {
                        jarSpec.include("META-INF/resources/webjars/**");
                        jarSpec.setIncludeEmptyDirs(false);
                        jarSpec.eachFile(fcd -> fcd.setPath(fcd.getPath().replaceFirst("META-INF/resources/webjars/(.*?)/(.*?)/", "$1/")));

                    })
            );
        });
    }
}
