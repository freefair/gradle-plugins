package io.freefair.gradle.plugins.maven.war;

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.copy.CopySpecInternal;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.War;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.gradle.api.plugins.BasePlugin.CLEAN_TASK_NAME;
import static org.gradle.api.plugins.JavaPlugin.*;
import static org.gradle.api.plugins.WarPlugin.WAR_TASK_NAME;

/**
 * @author Lars Grefer
 */
@NonNullApi
public class WarOverlayPlugin implements Plugin<Project> {

    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;
        project.getPluginManager().apply(WarPlugin.class);

        project.getTasks().withType(War.class, warTask -> {
            NamedDomainObjectContainer<WarOverlay> warOverlays = project.container(WarOverlay.class, name -> new WarOverlay(name, warTask));
            warTask.getExtensions().add("overlays", warOverlays);

            Configuration warOverlayClasspath = project.getConfigurations().create(warTask.getName() + "OverlayClasspath");
            warTask.getExtensions().add("overlayClasspath", warOverlayClasspath);

            if (warTask.getName().equals(WarPlugin.WAR_TASK_NAME)) {
                project.getConfigurations().getByName(COMPILE_CLASSPATH_CONFIGURATION_NAME).extendsFrom(warOverlayClasspath);
                project.getConfigurations().getByName(TEST_IMPLEMENTATION_CONFIGURATION_NAME).extendsFrom(warOverlayClasspath);
            }

            project.afterEvaluate(p -> warOverlays.all(overlay -> {

                if (overlay.isEnabled()) {
                    configureOverlay(overlay);
                } else {
                    Collection<CopySpecInternal> children = (Collection<CopySpecInternal>) overlay.getWarTask().getRootSpec().getChildren();
                    children.remove(overlay.getWarCopySpec());
                }

            }));
        });

    }

    private void configureOverlay(WarOverlay overlay) {

        if (overlay.isDeferProvidedConfiguration()) {
            //Delay this to trick IntelliJ
            //noinspection Convert2Lambda
            overlay.getWarTask().doFirst(new Action<Task>() {
                @Override
                public void execute(Task w) {
                    overlay.getWarCopySpec().exclude(element -> overlay.isProvided());
                }
            });
        } else {
            overlay.getWarCopySpec().exclude(element -> overlay.isProvided());
        }

        Object source = overlay.getSource();

        if (source instanceof AbstractArchiveTask) {
            configureOverlay(overlay, (AbstractArchiveTask) source);
        } else if (source instanceof Project && overlay.getConfigureClosure() == null) {
            configureOverlay(overlay, (Project) source);
        } else if (source instanceof File) {
            configureOverlay(overlay, (File) source);
        } else {
            Closure configClosure = overlay.getConfigureClosure();
            Dependency dependency;
            if (configClosure == null) {
                dependency = project.getDependencies().create(source);
            } else {
                dependency = project.getDependencies().create(source, configClosure);
            }

            if (dependency instanceof ProjectDependency) {
                configureOverlay(overlay, ((ProjectDependency) dependency).getDependencyProject());
            } else if (dependency instanceof ExternalDependency) {
                configureOverlay(overlay, (ExternalDependency) dependency);
            } else {
                throw new GradleException("Unsupported dependency type: " + dependency.getClass().getName());
            }
        }
    }

    private void configureOverlay(WarOverlay overlay, File file) {
        configureOverlay(overlay, () -> project.zipTree(file));
    }

    private void configureOverlay(WarOverlay overlay, ExternalDependency dependency) {
        War warTask = overlay.getWarTask();

        dependency.setTransitive(false);

        Configuration configuration = project.getConfigurations().create(overlay.getConfigurationName());
        configuration.setDescription(String.format("Contents of the overlay '%s' for the task '%s'.", overlay.getName(), warTask.getName()));
        configuration.getDependencies().add(dependency);

        configureOverlay(overlay, () -> project.zipTree(configuration.getSingleFile()));
    }

    private void configureOverlay(WarOverlay overlay, Callable<FileTree> warTree) {
        War warTask = overlay.getWarTask();

        String capitalizedWarTaskName = StringGroovyMethods.capitalize((CharSequence) warTask.getName());
        String capitalizedOverlayName = StringGroovyMethods.capitalize((CharSequence) overlay.getName());

        File destinationDir = new File(project.getBuildDir(), String.format("overlays/%s/%s", warTask.getName(), overlay.getName()));
        Action<CopySpec> extractOverlay = copySpec -> {
            copySpec.into(destinationDir);
            copySpec.from(warTree);
        };

        Sync extractOverlayTask = project.getTasks().create(String.format("extract%s%sOverlay", capitalizedOverlayName, capitalizedWarTaskName), Sync.class, extractOverlay);

        overlay.getWarCopySpec().from(extractOverlayTask);

        if (overlay.isEnableCompilation()) {

            if (!destinationDir.exists() || isEmpty(destinationDir)) {
                project.sync(extractOverlay);
            }

            project.getTasks().getByName(CLEAN_TASK_NAME).finalizedBy(extractOverlayTask);

            ConfigurableFileCollection classes = project.files(new File(destinationDir, "WEB-INF/classes"))
                    .builtBy(extractOverlayTask);

            project.getDependencies().add(getClasspathConfigurationName(overlay), classes);

            FileTree libs = project.files(extractOverlayTask).builtBy(extractOverlayTask).getAsFileTree()
                    .matching(patternFilterable -> patternFilterable.include("WEB-INF/lib/**"));

            project.getDependencies().add(getClasspathConfigurationName(overlay), libs);
        }
    }

    private boolean isEmpty(File destinationDir) {
        String[] list = destinationDir.list();
        return list == null || list.length == 0;
    }

    private void configureOverlay(WarOverlay overlay, Project otherProject) {
        project.evaluationDependsOn(otherProject.getPath());

        War otherWar = (War) otherProject.getTasks().getByName(WAR_TASK_NAME);

        configureOverlay(overlay, otherWar);

        if (overlay.isEnableCompilation()) {
            project.getDependencies().add(getClasspathConfigurationName(overlay), otherProject);

            otherProject.getPlugins().withType(WarOverlayPlugin.class, otherWarOverlayPlugin -> {
                Map<String, String> map = new HashMap<>();
                map.put("path", otherProject.getPath());
                map.put("configuration", otherWarOverlayPlugin.getClasspathConfigurationName(otherWar));

                project.getDependencies().add(
                        getClasspathConfigurationName(overlay),
                        project.getDependencies().project(map)
                );
            });
        }
    }

    private void configureOverlay(WarOverlay overlay, AbstractArchiveTask from) {
        overlay.getWarCopySpec().with(from.getRootSpec());
    }

    private String getClasspathConfigurationName(WarOverlay warOverlay) {
        return getClasspathConfigurationName(warOverlay.getWarTask());
    }

    private String getClasspathConfigurationName(War warTask) {
        return warTask.getName() + "OverlayClasspath";
    }

}
