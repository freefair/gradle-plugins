package io.freefair.gradle.plugins.plantuml;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;

/**
 * @author Lars Grefer
 */
@CacheableTask
public abstract class PlantumlTask extends SourceTask {

    @Inject
    protected abstract WorkerExecutor getWorkerExecutor();

    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();

    @Classpath
    public abstract ConfigurableFileCollection getPlantumlClasspath();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @Input
    @Optional
    public abstract Property<String> getFileFormat();

    @Input
    public abstract Property<Boolean> getWithMetadata();

    @Input
    public abstract Property<String> getIncludePattern();

    @Input
    public abstract Property<Boolean> getDeleteOutputBeforeBuild();

    @Input
    public abstract MapProperty<String, Object> getSystemProperties();

    @Input
    public abstract ListProperty<String> getJvmArgs();

    @Input
    @Optional
    public abstract Property<Boolean> getDebug();

    @OutputDirectory
    public abstract DirectoryProperty getTmpDir();

    public PlantumlTask() {
        this.setGroup("plantuml");
        getWithMetadata().convention(true);
        getIncludePattern().convention("**/*.puml");
        getDeleteOutputBeforeBuild().convention(true);
        getTmpDir().set(getTemporaryDir());
    }

    @TaskAction
    public void execute() {

        if (getDeleteOutputBeforeBuild().get()) {
            getFileSystemOperations().delete(deleteSpec -> deleteSpec.delete(getOutputDirectory()));
        }

        WorkQueue workQueue = getWorkerExecutor().processIsolation(process -> {
            process.getClasspath().from(getPlantumlClasspath());

            process.forkOptions(javaForkOptions -> {

                javaForkOptions.systemProperty("java.awt.headless", true);
                javaForkOptions.systemProperty("java.io.tmpdir", getTmpDir().get().getAsFile().getAbsolutePath());

                if (getSystemProperties().isPresent()) {
                    javaForkOptions.systemProperties(getSystemProperties().get());
                }

                if (getJvmArgs().isPresent()) {
                    javaForkOptions.jvmArgs(getJvmArgs().get());
                }

                if (getDebug().isPresent()) {
                    javaForkOptions.setDebug(getDebug().get());
                }

            });
        });

        getSource().matching(p -> p.include(getIncludePattern().get()))
                .visit(fileVisitDetails -> {
            if (fileVisitDetails.isDirectory()) {
                return;
            }

            workQueue.submit(PlantumlAction.class, params -> {
                params.getInputFile().set(fileVisitDetails.getFile());
                File outputFile = fileVisitDetails.getRelativePath().getFile(getOutputDirectory().get().getAsFile());
                params.getOutputDirectory().set(outputFile.getParentFile());
                params.getFileFormat().set(getFileFormat());
                params.getWithMetadata().set(getWithMetadata());
            });
        });
    }
}
