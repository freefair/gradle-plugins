package io.freefair.gradle.plugins;

import io.freefair.gradle.plugins.base.AbstractPlugin;
import lombok.Getter;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.FileTree;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.War;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

/**
 * @author Lars Grefer
 */
public class WarOverlayPlugin extends AbstractPlugin {

    @Getter
    private WarOverlayExtension warOverlay;

    @Override
    public void apply(final Project project) {
        super.apply(project);
        project.getPluginManager().apply(WarPlugin.class);

        warOverlay = new WarOverlayExtension();
        project.getExtensions().add("warOverlay", warOverlay);

        project.getTasks().withType(War.class, warTask -> {
            Task configTask = project.getTasks().create("configureOverlayFor" + capitalize((CharSequence) warTask.getName()));
            configTask.setGroup("war");
            configTask.setDescription("Configures the " + warTask.toString() + " to properly overlay all wars in its classpath");

            warTask.dependsOn(configTask);

            configTask.doFirst(configTask1 -> {
                Set<File> warFiles = new HashSet<>();

                for (File file : warTask.getClasspath().getFiles()) {
                    if (file.isFile() && file.getName().endsWith(".war")) {

                        configTask1.getLogger().info("Using {} as overlay", file.getName());

                        warTask.from(project.zipTree(file), overlayCopySpec -> {
                            overlayCopySpec.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
                            overlayCopySpec.exclude(warOverlay.getExcludes());
                        });

                        warFiles.add(file);
                    }
                }

                warTask.setClasspath(warTask.getClasspath().minus(project.files(warFiles)));
            });
        });

        project.afterEvaluate(p -> {
            if (warOverlay.isAttachClasses()) {
                attachClasses(p);
            }

            if (warOverlay.isAttachWebInfClasses()) {
                attachWebInfClasses(p,
                        project.getConfigurations().getByName("compile"),
                        project.getConfigurations().getByName("compileClasspath")
                );
                attachWebInfClasses(p,
                        project.getConfigurations().getByName("testCompile"),
                        project.getConfigurations().getByName("testCompileClasspath")
                );
            }

            if(warOverlay.isAttachWebInfLib()) {
                attachWebInfLib(p,
                        project.getConfigurations().getByName("compile"),
                        project.getConfigurations().getByName("compileClasspath")
                );
                attachWebInfLib(p,
                        project.getConfigurations().getByName("testCompile"),
                        project.getConfigurations().getByName("testCompileClasspath")
                );
            }
        });
    }

    private void attachWebInfClasses(Project project, Configuration source, Configuration target) {
        for (File resolvedFile : source.getResolvedConfiguration().getFiles()) {
            if (resolvedFile.getName().endsWith(".war")) {
                Jar classesJar = getClassesJarTask(project, resolvedFile);

                project.getDependencies().add(target.getName(), project.files(classesJar));
            }
        }
    }

    @Getter
    private Map<File, Jar> classesJarTasks = new HashMap<>();

    private Jar getClassesJarTask(Project project, File warFile) {

        if(classesJarTasks.containsKey(warFile)) {
            return classesJarTasks.get(warFile);
        }

        String name = warFile.getName().replace(".war", "");

        Jar classesJar = project.getTasks().create( name + "ClassesJar", Jar.class);

        Callable<FileTree> classFiles = () -> project.zipTree(warFile)
                .matching(patternFilterable -> patternFilterable.include("WEB-INF/classes/**"));

        classesJar.onlyIf(t -> {
            try {
                return !classFiles.call().isEmpty();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        classesJar.from(
                classFiles,
                classesCopySpec -> classesCopySpec.eachFile(f -> f.setPath(f.getPath().replace("WEB-INF/classes/", "")))
        );

        classesJar.setClassifier("classes");

        classesJar.setBaseName(name);
        classesJar.setGroup("war");
        classesJar.setDescription("Packages a jar containing the WEB-INF/classes directory of '" + warFile + "'");

        classesJarTasks.put(warFile, classesJar);

        return classesJar;
    }

    private void attachWebInfLib(Project project, Configuration source, Configuration target) {
        for (File resolvedFile : source.getResolvedConfiguration().getFiles()) {
            if (resolvedFile.getName().endsWith(".war")) {
                project.getDependencies().add(
                        target.getName(),
                        project.zipTree(resolvedFile)
                                .matching(patternFilterable -> patternFilterable.include("WEB-INF/lib/**"))
                );
            }
        }
    }

    private void attachClasses(Project project) {
        final AbstractArchiveTask jar = (AbstractArchiveTask) project.getTasks().getByName("jar");
        jar.setClassifier(warOverlay.getClassesClassifier());

        project.getArtifacts().add(Dependency.ARCHIVES_CONFIGURATION, jar);
    }

}
