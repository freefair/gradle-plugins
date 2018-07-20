package io.freefair.gradle.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.*;
import org.gradle.api.plugins.scala.ScalaBasePlugin;
import org.gradle.api.tasks.GroovySourceSet;
import org.gradle.api.tasks.ScalaSourceSet;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.scala.ScalaCompile;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class AspectJPostCompileWeavingPlugin implements Plugin<Project> {

    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;
        AspectJBasePlugin aspectjBasePlugin = project.getPlugins().apply(AspectJBasePlugin.class);

        project.getPlugins().apply(JavaBasePlugin.class);

        project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(sourceSet -> {
            project.getDependencies().add(sourceSet.getCompileConfigurationName(), "org.aspectj:aspectjrt:" + aspectjBasePlugin.getAspectjExtension().getVersion().get());

            AspectJSourceSet aspectJSourceSet = project.getObjects().newInstance(AspectJSourceSet.class);
            new DslObject(sourceSet).getConvention().getPlugins().put("aspectj", aspectJSourceSet);

            aspectJSourceSet.setAspectpathConfigurationName(sourceSet.getTaskName(null, "aspectpath"));
            aspectJSourceSet.setAspectpath(project.getConfigurations().create(aspectJSourceSet.getAspectpathConfigurationName()));

            project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
                JavaCompile compileJava = (JavaCompile) project.getTasks().getByName(sourceSet.getCompileJavaTaskName());

                createWeavingTask(compileJava, compileJava.getOptions(), sourceSet, sourceSet.getJava());
            });

            project.getPlugins().withType(GroovyBasePlugin.class, groovyPlugin -> {
                GroovyCompile compileGroovy = (GroovyCompile) project.getTasks().getByName(sourceSet.getCompileTaskName("groovy"));

                SourceDirectorySet sourceDirectorySet = new DslObject(sourceSet).getConvention().getPlugin(GroovySourceSet.class).getGroovy();

                createWeavingTask(compileGroovy, compileGroovy.getOptions(), sourceSet, sourceDirectorySet);
            });

            project.getPlugins().withType(ScalaBasePlugin.class, scalaBasePlugin -> {
                ScalaCompile compileScala = (ScalaCompile) project.getTasks().getByName(sourceSet.getCompileTaskName("scala"));

                SourceDirectorySet sourceDirectorySet = new DslObject(sourceSet).getConvention().getPlugin(ScalaSourceSet.class).getScala();

                createWeavingTask(compileScala, compileScala.getOptions(), sourceSet, sourceDirectorySet);
            });

        });
    }

    private void createWeavingTask(
            AbstractCompile abstractCompile,
            @Nullable CompileOptions compileOptions,
            SourceSet sourceSet,
            SourceDirectorySet sourceDirectorySet
    ) {
        String taskName = sourceSet.getTaskName("weave", sourceDirectorySet.getName());

        Ajc ajc = project.getTasks().create(taskName, Ajc.class);

        File temporaryDir = ajc.getTemporaryDir();

        abstractCompile.setDestinationDir(temporaryDir);

        ajc.getSource().set(project.provider(abstractCompile::getSourceCompatibility));
        ajc.getTarget().set(project.provider(abstractCompile::getTargetCompatibility));
        ajc.getClasspath().from(abstractCompile.getClasspath());
        ajc.getAspectpath().from(new DslObject(sourceSet).getConvention().getPlugin(AspectJSourceSet.class).getAspectpath());
        ajc.getInpath().from(abstractCompile.getDestinationDir()).builtBy(abstractCompile);
        ajc.getDestinationDir().set(sourceSet.getJava().getOutputDir());

        if (compileOptions != null) {
            ajc.getEncoding().set(project.provider(compileOptions::getEncoding));
            ajc.getDeprecation().set(project.provider(compileOptions::isDeprecation));
            ajc.getVerbose().set(project.provider(compileOptions::isVerbose));
            ajc.getBootclasspath().from(project.provider(() ->
                    Optional.ofNullable(compileOptions.getBootstrapClasspath()).orElse(project.files())
            ));
            ajc.getExtdirs().from(project.provider(() ->
                    Optional.ofNullable(compileOptions.getExtensionDirs())
                    .map(path -> project.files((Object[]) path.split(File.pathSeparator)))
                    .orElse(project.files())
            ));
        }

        project.afterEvaluate(p -> project.getTasks().all(task -> {
            if (task.getDependsOn().contains(abstractCompile)
                    || task.getDependsOn().contains(abstractCompile.getPath())
                    || task.getDependsOn().contains(abstractCompile.getName())) {
                task.dependsOn(ajc);
            }
        }));
    }
}
