package io.freefair.gradle.plugins.plantuml;

import lombok.Getter;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;

/**
 * @author Lars Grefer
 */
public class PlantumlTask extends SourceTask {

    private final WorkerExecutor workerExecutor;

    @Getter
    @Classpath
    private final ConfigurableFileCollection plantumlClasspath = getProject().files();

    @Getter
    @OutputDirectory
    private final DirectoryProperty outputDirectory = getProject().getObjects().directoryProperty();

    @Getter
    @Input
    private final Property<String> fileFormat = getProject().getObjects().property(String.class).convention("SVG");

    @Getter
    @Input
    private final Property<Boolean> withMetadata = getProject().getObjects().property(Boolean.class).convention(true);

    @Inject
    public PlantumlTask(WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor;
    }

    @TaskAction
    public void execute() {

        getProject().delete(outputDirectory);

        for (File file : getSource()) {
            workerExecutor.classLoaderIsolation(iso -> iso.getClasspath().from(plantumlClasspath))
                    .submit(PlantumlAction.class, params -> {
                        params.getInputFile().set(file);
                        params.getOutputDirectory().set(outputDirectory);
                        params.getFileFormat().set(fileFormat);
                        params.getWithMetadata().set(withMetadata);
                    });
        }
    }
}
