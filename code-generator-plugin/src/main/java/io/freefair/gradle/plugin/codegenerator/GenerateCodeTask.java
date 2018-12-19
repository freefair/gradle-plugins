package io.freefair.gradle.plugin.codegenerator;

import io.freefair.gradle.codegenerator.api.Generator;
import io.freefair.gradle.codegenerator.api.ProjectContext;
import io.freefair.gradle.codegenerator.api.annotations.CodeGenerator;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.compile.AbstractCompile;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class GenerateCodeTask extends DefaultTask {

    @Setter
    private File inputDir;

    @Setter
    private File outputDir;

    @Setter
    private CodeGeneratorConfiguration extension;

    @Setter
    private Configuration codeGeneratorConfiguration;

    @TaskAction
    public void generate() {

        Set<File> resolve = Collections.singleton(null);
        try {
            Set<File> resolve1 = codeGeneratorConfiguration.resolve();
            log.debug("Resolved files: " + resolve1.size() + " (" + resolve1.stream().map(File::getName).collect(Collectors.joining(", ")) + ")");
            ResolvedConfiguration resolvedConfiguration = codeGeneratorConfiguration.getResolvedConfiguration();

            resolve = new HashSet<>(resolvedConfiguration.getFiles());
            log.debug("Found files: " + resolve.size() + " (" + resolve.stream().map(File::getName).collect(Collectors.joining(", ")) + ")");
            Set<ResolvedArtifact> resolvedArtifacts = resolvedConfiguration.getResolvedArtifacts();

            resolve.addAll(resolvedArtifacts.stream().map(ResolvedArtifact::getFile).collect(Collectors.toList()));
            log.debug("Found artifacts: " + resolvedArtifacts.size() + " (" + resolvedArtifacts.stream().map(a -> a.getName() + ":" + a.getType()).collect(Collectors.joining(", ")) + ")");
        } catch (Exception ex) {
            log.debug("Error while resolving compileOnly", ex);
        }
        List<URL> urls = Stream.concat(extension.getGeneratorJars().stream(), resolve.stream()).map(c -> {
            try {
                if(log.isDebugEnabled())
                    log.debug("File: " + c.getPath());
                File file;
                if (!c.getPath().startsWith("/"))
                    file = new File(getProject().getProjectDir().getAbsolutePath(), c.getPath());
                else
                    file = c;
                if (file.exists())
                    return file.toURI().toURL();
                if(log.isDebugEnabled())
                    log.debug("... File does not exist");
                return null;
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());


        if(log.isDebugEnabled()) {
            log.debug("Found " + urls.size() + " urls to scan: ");
            log.debug(urls.stream().map(URL::getPath).collect(Collectors.joining(",")));
        }

        ClassLoader loader = new URLClassLoader(urls.toArray(URL[]::new), Thread.currentThread().getContextClassLoader());
        ScanResult scan = new ClassGraph()
                .overrideClasspath(urls)
                .enableClassInfo()
                .enableAnnotationInfo()
                .scan();
        ClassInfoList classesWithAnnotation = scan.getClassesWithAnnotation(CodeGenerator.class.getCanonicalName());
        ClassInfoList classesImplementing = scan.getClassesImplementing(Generator.class.getCanonicalName());

        if(log.isDebugEnabled()) {
            log.debug("Found " + classesWithAnnotation.size() + " with code generator annotation (" + CodeGenerator.class.getCanonicalName() +  "): ");
            log.debug(classesWithAnnotation.stream().map(ClassInfo::getName).collect(Collectors.joining(",")));
            log.debug("Found " + classesImplementing.size() + " implementing " + Generator.class.getCanonicalName() + ": ");
            log.debug(classesImplementing.stream().map(ClassInfo::getName).collect(Collectors.joining(",")));
        }

        ProjectContext context = new ProjectContext(getProject().getProjectDir(), inputDir, outputDir, extension.getConfigurationValues());

        classesWithAnnotation.forEach(c -> {
            try {
                log.debug("Executing " + c.getName() + " ...");
                new CodeGeneratorExecutor(loader.loadClass(c.getName())).execute(context);
                log.debug("... Success");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}
