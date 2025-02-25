package io.freefair.gradle.plugins.plantuml;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.JavaForkOptions;
import org.gradle.process.internal.JavaForkOptionsFactory;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;

/**
 * @author Lars Grefer
 */
public abstract class PlantumlTask extends SourceTask {

    @Inject
    protected abstract WorkerExecutor getWorkerExecutor();

    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();

    @Inject
    protected abstract JavaForkOptionsFactory getJavaForkOptionsFactory();

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
    public abstract Property<Boolean> getDebug();

    @Input
    public abstract Property<String> getTmpDir();

    public PlantumlTask() {
        this.setGroup("plantuml");
        getWithMetadata().convention(true);
        getIncludePattern().convention("**/*.puml");
        getDeleteOutputBeforeBuild().convention(true);
    }

    private JavaForkOptions getForkOptions() {
        JavaForkOptions forkOptions = getJavaForkOptionsFactory().newJavaForkOptions();
        forkOptions.systemProperty("java.awt.headless", true);
        getSystemProperties().get().forEach(forkOptions::systemProperty);
        getJvmArgs().get().forEach(forkOptions::jvmArgs);
        if (getDebug().isPresent() && getDebug().get()) {
            forkOptions.setDebug(true);
        }
        if(getTmpDir().isPresent()) {
            forkOptions.systemProperty("java.io.tmpdir", getTmpDir().get());
        } else {
            forkOptions.systemProperty("java.io.tmpdir", System.getProperty("java.io.tmpdir"));
        }
        return forkOptions;
    }

    @TaskAction
    public void execute() {

        if (getDeleteOutputBeforeBuild().get()) {
            getFileSystemOperations().delete(deleteSpec -> deleteSpec.delete(getOutputDirectory()));
        }

        WorkQueue workQueue = getWorkerExecutor().processIsolation(process -> {
            process.getClasspath().from(getPlantumlClasspath());
            getForkOptions().copyTo(process.getForkOptions());
        });

        for (File file : getSource().matching(p -> p.include(getIncludePattern().get()))) {
            workQueue.submit(PlantumlAction.class, params -> {
                params.getInputFile().set(file);
                params.getOutputDirectory().set(getOutputDirectory());
                params.getFileFormat().set(getFileFormat());
                params.getWithMetadata().set(getWithMetadata());
            });
        }
    }
}
