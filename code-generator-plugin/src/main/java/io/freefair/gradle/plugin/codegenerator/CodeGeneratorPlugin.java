package io.freefair.gradle.plugin.codegenerator;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;

public class CodeGeneratorPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        CodeGeneratorConfiguration codeGenerator = project.getExtensions().create("codeGenerator", CodeGeneratorConfiguration.class, project.getObjects());
        Configuration codeGeneratorConfiguration = project.getConfigurations().create("codeGenerator");

        JavaPluginConvention plugin = project.getConvention().getPlugin(JavaPluginConvention.class);
        for (SourceSet sourceSet : plugin.getSourceSets()) {
            String outputDir = project.getBuildDir() + "/generated-src/generator/" + sourceSet.getName();
            File outputDirFile = new File(outputDir);
            project.getLogger().debug("Using output dir {}", outputDir);

            File inputDir = new File(project.getProjectDir() + "/src/code-generator/" + sourceSet.getName());
            sourceSet.getJava().srcDir(inputDir);
            sourceSet.getJava().srcDir(outputDirFile);

            project.getLogger().debug("Using input dir {}", inputDir);

            String taskName = sourceSet.getTaskName("generate", "Code");

            TaskProvider<GenerateCodeTask> generate = project.getTasks().register(taskName, GenerateCodeTask.class, s -> {
                s.setGroup("generate");
                s.getOutputDir().set(outputDirFile);
                if(inputDir.isDirectory()) {
                    s.getInputDir().set(inputDir);
                }
                s.getCodeGeneratorClasspath().from(codeGeneratorConfiguration);
                s.getConfigurationValues().set(codeGenerator.getConfigurationValues());
                s.dependsOn(codeGeneratorConfiguration);
            });
            project.getTasks().named(sourceSet.getCompileJavaTaskName(), t -> t.dependsOn(generate));
        }
    }
}
