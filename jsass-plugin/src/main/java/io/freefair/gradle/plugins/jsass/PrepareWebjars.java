package io.freefair.gradle.plugins.jsass;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.*;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.File;

@Getter
@Setter
@Deprecated
public class PrepareWebjars extends DefaultTask {

    @Getter(AccessLevel.NONE)
    private final FileSystemOperations fileSystemOperations;
    @Getter(AccessLevel.NONE)
    private final ArchiveOperations archiveOperations;

    @OutputDirectory
    private final DirectoryProperty outputDirectory = getProject().getObjects().directoryProperty();

    @InputFiles
    @PathSensitive(PathSensitivity.NAME_ONLY)
    private final ConfigurableFileCollection webjars = getProject().files();

    @Inject
    public PrepareWebjars(FileSystemOperations fileSystemOperations, ArchiveOperations archiveOperations) {
        this.fileSystemOperations = fileSystemOperations;
        this.archiveOperations = archiveOperations;
    }

    @TaskAction
    public void extractWebjars() {
        fileSystemOperations.sync(sync -> {
            sync.into(outputDirectory);

            sync.setDuplicatesStrategy(DuplicatesStrategy.WARN);

            webjars.filter(File::isFile).getFiles().forEach(file ->
                    sync.from(archiveOperations.zipTree(file), jarSpec -> {
                        jarSpec.include("META-INF/resources/webjars/**");
                        jarSpec.setIncludeEmptyDirs(false);
                        jarSpec.eachFile(fcd -> fcd.setPath(fcd.getPath().replaceFirst("META-INF/resources/webjars/(.*?)/(.*?)/", "$1/")));

                    })
            );
        });
    }
}
