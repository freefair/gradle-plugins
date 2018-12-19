package io.freefair.gradle.plugin.codegenerator;

import io.freefair.gradle.codegenerator.api.Generator;
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
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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

            project.getTasks().create(taskName, GenerateCodeTask.class, s -> {
                s.setGroup("generate");
                s.setOutputDir(outputDirFile);
                s.setInputDir(inputDir);
                s.setCodeGeneratorConfiguration(codeGeneratorConfiguration);
                s.setExtension(codeGenerator);
                s.dependsOn(codeGeneratorConfiguration);
                project.getTasks().getByName(sourceSet.getCompileJavaTaskName()).dependsOn(s.getPath());
            });
        }
    }
}
