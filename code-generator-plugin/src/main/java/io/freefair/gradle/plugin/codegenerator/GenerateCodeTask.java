package io.freefair.gradle.plugin.codegenerator;

import io.freefair.gradle.codegenerator.api.Generator;
import io.freefair.gradle.codegenerator.api.ProjectContext;
import io.freefair.gradle.codegenerator.api.annotations.CodeGenerator;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.util.Collections;
import java.util.stream.Collectors;

@Getter
@Setter
public class GenerateCodeTask extends DefaultTask {

    @Getter(AccessLevel.NONE)
    private final ProjectLayout projectLayout;
    @Getter(AccessLevel.NONE)
    private final WorkerExecutor workerExecutor;

    @InputDirectory
    @Optional
    private final DirectoryProperty inputDir = getProject().getObjects().directoryProperty();

    @OutputDirectory
    private final DirectoryProperty outputDir = getProject().getObjects().directoryProperty();

    @Input
    @Optional
    private final MapProperty<String, Object> configurationValues = getProject().getObjects().mapProperty(String.class, Object.class);

    @Input
    private final Property<String> sourceSet = getProject().getObjects().property(String.class);

    @InputFiles
    @Classpath
    private final ConfigurableFileCollection codeGeneratorClasspath = getProject().files();

    @Inject
    public GenerateCodeTask(ProjectLayout projectLayout, WorkerExecutor workerExecutor) {
        this.projectLayout = projectLayout;
        this.workerExecutor = workerExecutor;
    }

    @TaskAction
    public void generate() {

        ScanResult scan = new ClassGraph()
                .overrideClasspath(codeGeneratorClasspath)
                .enableClassInfo()
                .enableAnnotationInfo()
                .scan();
        ClassInfoList classesWithAnnotation = scan.getClassesWithAnnotation(CodeGenerator.class.getCanonicalName());
        ClassInfoList classesImplementing = scan.getClassesImplementing(Generator.class.getCanonicalName());

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Found {} with code generator annotation ({}): ", classesWithAnnotation.size(), CodeGenerator.class.getCanonicalName());
            getLogger().debug(classesWithAnnotation.stream().map(ClassInfo::getName).collect(Collectors.joining(",")));
            getLogger().debug("Found {} implementing {}: ", classesImplementing.size(), Generator.class.getCanonicalName());
            getLogger().debug(classesImplementing.stream().map(ClassInfo::getName).collect(Collectors.joining(",")));
        }

        ProjectContext context = new ProjectContext(projectLayout.getProjectDirectory().getAsFile(), inputDir.getAsFile().getOrElse(this.getTemporaryDir()), outputDir.getAsFile().get(), configurationValues.getOrElse(Collections.emptyMap()), sourceSet.getOrElse("none"));

        WorkQueue workQueue = workerExecutor.classLoaderIsolation(spec -> spec.getClasspath().from(codeGeneratorClasspath));

        for (ClassInfo classInfo : classesWithAnnotation) {
            workQueue.submit(UnitOfWork.class, parameters -> {
                parameters.getClassName().set(classInfo.getName());
                parameters.getProjectContext().set(context);
            });
        }
    }

    interface Parameters extends WorkParameters {
        Property<String> getClassName();

        Property<ProjectContext> getProjectContext();
    }

    @Slf4j
    @RequiredArgsConstructor(onConstructor_ = @Inject)
    abstract static class UnitOfWork implements WorkAction<Parameters> {

        @Override
        @SneakyThrows
        public void execute() {
            String className = getParameters().getClassName().get();

            log.info("Executing {} ...", className);
            new CodeGeneratorExecutor(Class.forName(className)).execute(getParameters().getProjectContext().get());
            log.debug("... Success");
        }
    }
}
