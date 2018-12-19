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
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.IsolationMode;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.util.Collections;
import java.util.stream.Collectors;

@Getter
@Setter
public class GenerateCodeTask extends DefaultTask {

    private final WorkerExecutor workerExecutor;

    @InputDirectory
    private final DirectoryProperty inputDir = getProject().getObjects().directoryProperty();

    @OutputDirectory
    private final DirectoryProperty outputDir = getProject().getObjects().directoryProperty();

    @Input
    @Optional
    private final MapProperty<String, Object> configurationValues = getProject().getObjects().mapProperty(String.class, Object.class);

    @InputFiles
    @Classpath
    private final ConfigurableFileCollection codeGeneratorClasspath = getProject().files();

    @Inject
    public GenerateCodeTask(WorkerExecutor workerExecutor) {
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

        ProjectContext context = new ProjectContext(getProject().getProjectDir(), inputDir.getAsFile().get(), outputDir.getAsFile().get(), configurationValues.getOrElse(Collections.emptyMap()));

        for (ClassInfo classInfo : classesWithAnnotation) {
            workerExecutor.submit(UnitOfWork.class, workerConfiguration -> {
                workerConfiguration.setDisplayName(classInfo.getName());
                workerConfiguration.setIsolationMode(IsolationMode.CLASSLOADER);
                workerConfiguration.classpath(codeGeneratorClasspath);
                workerConfiguration.params(classInfo.getName(), context);
            });
        }
    }

    @Slf4j
    @RequiredArgsConstructor(onConstructor_ = @Inject)
    static class UnitOfWork implements Runnable {
        private final String className;
        private final ProjectContext projectContext;

        @Override
        @SneakyThrows
        public void run() {
            log.info("Executing {} ...", className);
            new CodeGeneratorExecutor(Class.forName(className)).execute(projectContext);
            log.debug("... Success");
        }
    }
}
