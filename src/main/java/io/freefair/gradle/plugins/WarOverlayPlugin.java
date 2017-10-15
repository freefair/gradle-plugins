package io.freefair.gradle.plugins;

import io.freefair.gradle.plugins.base.AbstractPlugin;
import lombok.Getter;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.FileTree;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.War;

import java.io.File;
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

        warOverlay = new WarOverlayExtension(project);
        project.getExtensions().add("warOverlay", warOverlay);

        project.getTasks().withType(War.class, warTask -> {
            Task configTask = project.getTasks().create("configureOverlayFor" + capitalize((CharSequence) warTask.getName()));
            configTask.setGroup("war");

            warTask.dependsOn(configTask);

            configTask.doFirst(configTask1 -> {
                for (File file : warTask.getClasspath().getFiles()) {
                    if (file.isFile() && file.getName().endsWith(".war")) {

                        configTask1.getLogger().info("Using {} as overlay", file.getName());

                        warTask.from(project.zipTree(file), overlayCopySpec -> {
                            overlayCopySpec.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
                            overlayCopySpec.exclude(warOverlay.getExcludes());
                        });

                        warTask.getRootSpec().exclude("**/" + file.getName());
                    }
                }
            });

            configTask.onlyIf(element -> warOverlay.isEnabled());
        });

        project.afterEvaluate(p -> {
            if (warOverlay.isAttachClasses()) {
                attachClasses(p);
            }

            Configuration classesSource = warOverlay.getWebInfClassesSource();
            Configuration classesTarget = warOverlay.getWebInfClassesTarget();

            if (classesSource != null && classesTarget != null) {
                attachWebInfClasses(p, classesSource, classesTarget);
            }

            Configuration libSource = warOverlay.getWebInfLibSource();
            Configuration libTarget = warOverlay.getWebInfLibTarget();

            if(libSource != null && libTarget != null) {
                attachWebInfLib(p, libSource, libTarget);
            }
        });
    }

    private void attachWebInfClasses(final Project project, Configuration source, Configuration target) {
        for (final ResolvedArtifact resolvedArtifact : source.getResolvedConfiguration().getResolvedArtifacts()) {
            if (resolvedArtifact.getExtension().equals("war")) {
                String name = resolvedArtifact.getModuleVersion().getId().getName();
                String group = resolvedArtifact.getModuleVersion().getId().getGroup();
                String version = resolvedArtifact.getModuleVersion().getId().getVersion();

                Jar classesJar = project.getTasks().create(group + name + version + "classesJar", Jar.class);

                classesJar.from(
                        (Callable<FileTree>) () -> project
                                .fileTree(resolvedArtifact.getFile())
                                .matching(patternFilterable -> patternFilterable.include("WEB-INF/classes/**")),
                        classesCopySpec -> classesCopySpec.eachFile(f -> f.setPath(f.getPath().replace("WEB-INF/classes/", "")))
                );

                classesJar.setClassifier("classes");

                classesJar.setBaseName(name);
                classesJar.setVersion(version);
                classesJar.setGroup("war");
                classesJar.setDescription("Packages a jar containing the WEB-INF/classes directory of '" + resolvedArtifact.getId().getComponentIdentifier().getDisplayName() + "'");

                project.getDependencies().add(target.getName(), project.files(classesJar));
            }
        }
    }

    private void attachWebInfLib(Project project, Configuration source, Configuration target) {
        for (ResolvedArtifact resolvedArtifact : source.getResolvedConfiguration().getResolvedArtifacts()) {
            if (resolvedArtifact.getExtension().equals("war")) {
                project.getDependencies().add(
                        target.getName(),
                        (Callable<FileTree>) () -> project.zipTree(resolvedArtifact.getFile())
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
