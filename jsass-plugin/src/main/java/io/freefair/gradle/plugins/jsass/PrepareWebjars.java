package io.freefair.gradle.plugins.jsass;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

@Getter
@Setter
public class PrepareWebjars extends DefaultTask {

    @OutputDirectory
    private final DirectoryProperty outputDirectory= newOutputDirectory();

    @InputFiles
    private Configuration configuration;

    @TaskAction
    public void extractWebjars() {
        getProject().sync(sync -> {
            sync.into(outputDirectory);

            sync.setDuplicatesStrategy(DuplicatesStrategy.WARN);
            for (ResolvedArtifact resolvedArtifact : configuration.getResolvedConfiguration().getResolvedArtifacts()) {
                if (resolvedArtifact.getModuleVersion().getId().getGroup().startsWith("org.webjars")) {
                    sync.from(getProject().zipTree(resolvedArtifact.getFile()), jarSpec -> {
                        jarSpec.include("META-INF/resources/webjars/**");
                        jarSpec.setIncludeEmptyDirs(false);
                        jarSpec.eachFile(fcd -> fcd.setPath(fcd.getPath().replaceFirst("META-INF/resources/webjars/(.*?)/(.*?)/", "$1/")));
                    });
                }
            }
        });
    }
}
