package io.freefair.gradle.plugin.codegenerator;

import io.freefair.gradle.codegenerator.api.ProjectContext;
import io.freefair.gradle.codegenerator.api.annotations.CodeGenerator;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CodeGeneratorPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        CodeGeneratorConfiguration codeGenerator = project.getExtensions().create("codeGenerator", CodeGeneratorConfiguration.class);
        Configuration codeGeneratorConfiguration = project.getConfigurations().create("codeGenerator");

        JavaPluginConvention plugin = project.getConvention().getPlugin(JavaPluginConvention.class);
        for (SourceSet sourceSet : plugin.getSourceSets()) {
            String outputDir = project.getBuildDir() + "/generated-src/generator/" + sourceSet.getName();
            File outputDirFile = new File(outputDir);
            if(log.isDebugEnabled()) {
                log.debug("Using output dir " + outputDir);
            }

            File inputDir = new File(project.getProjectDir() + "/src/code-generator/" + sourceSet.getName());
            sourceSet.getJava().srcDir(inputDir);

            if(log.isDebugEnabled()) {
                log.debug("Using input dir " + inputDir);
            }

            String taskName = sourceSet.getTaskName("generate", "Code");

            project.getTasks().create(taskName, t -> {
                project.getTasks().getByName(sourceSet.getCompileJavaTaskName()).dependsOn(t.getPath());
                t.doLast(t2 -> {
                    Set<File> resolve = Collections.singleton(null);
                    try {
                        resolve = codeGeneratorConfiguration.resolve();
                    } catch (Exception ex) {
                        log.debug("Error while resolving compileOnly", ex);
                    }
                    URL[] urls = Stream.concat(codeGenerator.getGeneratorJars().stream(), resolve.stream()).map(c -> {
                        try {
                            File file;
                            if (!c.getPath().startsWith("/"))
                                file = new File(project.getProjectDir().getAbsolutePath(), c.getPath());
                            else
                                file = c;
                            if (file.exists())
                                return file.toURI().toURL();
                            return null;
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    }).filter(Objects::nonNull).toArray(URL[]::new);

                    if(log.isDebugEnabled()) {
                        log.debug("Found " + urls.length + " urls to scan");
                    }

                    ClassLoader loader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
                    ClassLoader scanningLoader = new URLClassLoader(urls);
                    Thread.currentThread().setContextClassLoader(loader);

                    ScanResult scan = new ClassGraph()
                                            .overrideClassLoaders(scanningLoader)
                                            .enableClassInfo()
                                            .enableAnnotationInfo()
                                            .blacklistPackages("org.gradle")
                                            .scan();
                    ClassInfoList classesWithAnnotation = scan.getClassesWithAnnotation(CodeGenerator.class.getCanonicalName());

                    if(log.isDebugEnabled()) {
                        log.debug("Found " + classesWithAnnotation.size() + " with code generator annotation: ");
                        log.debug(classesWithAnnotation.stream().map(ClassInfo::getName).collect(Collectors.joining(",")));
                    }

                    ProjectContext context = new ProjectContext(project.getProjectDir(), inputDir, outputDirFile, codeGenerator.getConfigurationValues());

                    classesWithAnnotation.forEach(c -> {
                        try {
                            log.debug("Executing " + c.getName() + " ...");
                            new CodeGeneratorExecutor(loader.loadClass(c.getName())).execute(context);
                            log.debug("... Success");
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                });
            });
        }
    }
}
