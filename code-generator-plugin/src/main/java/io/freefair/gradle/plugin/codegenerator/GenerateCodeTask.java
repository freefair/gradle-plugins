package io.freefair.gradle.plugin.codegenerator;

import io.freefair.gradle.codegenerator.api.Generator;
import io.freefair.gradle.codegenerator.api.ProjectContext;
import io.freefair.gradle.codegenerator.api.annotations.CodeGenerator;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
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
public abstract class GenerateCodeTask extends DefaultTask {

    @Inject
    protected abstract ProjectLayout getProjectLayout();
    @Inject
    protected abstract WorkerExecutor getWorkerExecutor();

    @InputDirectory
    @Optional
    public abstract DirectoryProperty getInputDir();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @Input
    @Optional
    public abstract MapProperty<String, Object> getConfigurationValues();

    @Input
    public abstract Property<String> getSourceSet();

    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getCodeGeneratorClasspath();

    @TaskAction
    public void generate() {

        ScanResult scan = new ClassGraph()
                .overrideClasspath(getCodeGeneratorClasspath())
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

        ProjectContext context = new ProjectContext(getProjectLayout().getProjectDirectory().getAsFile(), getInputDir().getAsFile().getOrElse(this.getTemporaryDir()), getOutputDir().getAsFile().get(), getConfigurationValues().getOrElse(Collections.emptyMap()), getSourceSet().getOrElse("none"));

        WorkQueue workQueue = getWorkerExecutor().classLoaderIsolation(spec -> spec.getClasspath().from(getCodeGeneratorClasspath()));

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
