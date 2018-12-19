package io.freefair.gradle.plugin.codegenerator;

import io.freefair.gradle.codegenerator.api.Generator;
import io.freefair.gradle.codegenerator.api.ProjectContext;
import io.freefair.gradle.codegenerator.api.annotations.CodeGenerator;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class GenerateCodeTask extends DefaultTask {

    @InputDirectory
    private final DirectoryProperty inputDir = getProject().getObjects().directoryProperty();

    @InputDirectory
    private final DirectoryProperty outputDir = getProject().getObjects().directoryProperty();

    @Input
    @Optional
    private final MapProperty<String, Object> configurationValues = getProject().getObjects().mapProperty(String.class, Object.class);

    @InputFiles
    @Classpath
    private final ConfigurableFileCollection codeGeneratorClasspath = getProject().files();

    @TaskAction
    public void generate() throws Exception {

        Set<File> resolve = codeGeneratorClasspath.getFiles();

        getLogger().debug("Resolved files: {} ({})", resolve.size(), resolve.stream().map(File::getName).collect(Collectors.joining(", ")));

        List<URL> urls = resolve.stream().map(c -> {
            try {
                getLogger().debug("File: {}", c.getPath());
                File file;
                if (!c.getPath().startsWith("/"))
                    file = new File(getProject().getProjectDir().getAbsolutePath(), c.getPath());
                else
                    file = c;
                if (file.exists())
                    return file.toURI().toURL();
                getLogger().debug("... File does not exist");
                return null;
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Found {} urls to scan: ", urls.size());
            getLogger().debug(urls.stream().map(URL::getPath).collect(Collectors.joining(",")));
        }

        ClassLoader loader = new URLClassLoader(urls.toArray(URL[]::new), Thread.currentThread().getContextClassLoader());
        ScanResult scan = new ClassGraph()
                .overrideClasspath(urls)
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

        for (ClassInfo c : classesWithAnnotation) {
            getLogger().info("Executing {} ...", c.getName());
            new CodeGeneratorExecutor(loader.loadClass(c.getName())).execute(context);
            getLogger().debug("... Success");
        }
    }
}
