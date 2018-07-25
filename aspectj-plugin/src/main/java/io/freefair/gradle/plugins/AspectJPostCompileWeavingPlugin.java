package io.freefair.gradle.plugins;

import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.GroovyBasePlugin;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.scala.ScalaBasePlugin;
import org.gradle.api.tasks.ClasspathNormalizer;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.scala.ScalaCompile;

@NonNullApi
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

            aspectJSourceSet.setAspectConfigurationName(sourceSet.getTaskName(null, "aspect"));
            Configuration aspectpath = project.getConfigurations().create(aspectJSourceSet.getAspectConfigurationName());
            aspectJSourceSet.setAspectPath(aspectpath);

            project.getConfigurations().getByName(sourceSet.getCompileConfigurationName())
                    .extendsFrom(aspectpath);

            project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
                JavaCompile compileJava = (JavaCompile) project.getTasks().getByName(sourceSet.getCompileJavaTaskName());

                enhanceWithWeavingAction(compileJava, aspectpath, aspectjBasePlugin.getAspectjConfiguration());
            });

            project.getPlugins().withType(GroovyBasePlugin.class, groovyPlugin -> {
                GroovyCompile compileGroovy = (GroovyCompile) project.getTasks().getByName(sourceSet.getCompileTaskName("groovy"));

                enhanceWithWeavingAction(compileGroovy, aspectpath, aspectjBasePlugin.getAspectjConfiguration());
            });

            project.getPlugins().withType(ScalaBasePlugin.class, scalaBasePlugin -> {
                ScalaCompile compileScala = (ScalaCompile) project.getTasks().getByName(sourceSet.getCompileTaskName("scala"));

                enhanceWithWeavingAction(compileScala, aspectpath, aspectjBasePlugin.getAspectjConfiguration());
            });
        });

        project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(sourceSet -> {
            project.getDependencies().add(sourceSet.getCompileConfigurationName(), "org.aspectj:aspectjrt:" + aspectjBasePlugin.getAspectjExtension().getVersion().get());

            Configuration aspects = project.getConfigurations().create(sourceSet.getTaskName(null, "aspects"));

            project.getConfigurations().getByName(sourceSet.getCompileConfigurationName()).extendsFrom(aspects);

            project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
                ConfigurableFileCollection aspectpath = (ConfigurableFileCollection) project.getTasks().getByName(sourceSet.getCompileJavaTaskName())
                        .getExtensions().getByName("aspectpath");
                aspectpath.from(aspects);
            });
            project.getPlugins().withType(GroovyPlugin.class, javaPlugin -> {
                ConfigurableFileCollection aspectpath = (ConfigurableFileCollection) project.getTasks().getByName(sourceSet.getCompileTaskName("groovy"))
                        .getExtensions().getByName("aspectpath");
                aspectpath.from(aspects);
            });

        });
    }

    private void enhanceWithWeavingAction(AbstractCompile abstractCompile, Configuration aspectpath, Configuration aspectjConfiguration) {
        AjcAction action = project.getObjects().newInstance(AjcAction.class);

        abstractCompile.doLast("ajc", action);
        abstractCompile.getExtensions().add("ajc", action);

        action.getAspectpath().from(aspectpath);
        action.getClasspath().from(aspectjConfiguration);

        abstractCompile.getInputs().files(action.getClasspath())
                .withPropertyName("aspectjClasspath")
                .withNormalizer(ClasspathNormalizer.class)
                .optional(false);

        abstractCompile.getInputs().files(action.getAspectpath())
                .withPropertyName("aspectpath")
                .withNormalizer(ClasspathNormalizer.class)
                .optional(true);

        abstractCompile.getInputs().property("ajcArgs", action.getCompilerArgs())
                .optional(true);

        abstractCompile.getInputs().property("ajcEnabled", action.getEnabled())
                .optional(true);
    }

}
