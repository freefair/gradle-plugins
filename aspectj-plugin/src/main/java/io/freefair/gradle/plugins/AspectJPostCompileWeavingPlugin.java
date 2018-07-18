package io.freefair.gradle.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.GroovyPlugin;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.language.scala.tasks.AbstractScalaCompile;

import java.util.LinkedList;
import java.util.List;

public class AspectJPostCompileWeavingPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        AspectJBasePlugin aspectjBasePlugin = project.getPlugins().apply(AspectJBasePlugin.class);

        project.getPlugins().apply(JavaBasePlugin.class);

        project.getTasks().withType(AbstractCompile.class, abstractCompile -> {

            List<String> ajcArgs = new LinkedList<>();
            abstractCompile.getExtensions().add("ajcArgs", ajcArgs);
            abstractCompile.getInputs().property("ajcArgs", ajcArgs);

            ConfigurableFileCollection aspectpath = project.files();
            abstractCompile.getExtensions().add("aspectpath", aspectpath);
            abstractCompile.getInputs().files(aspectpath).withPropertyName("aspectpath").optional();

            abstractCompile.doLast("ajc", t -> {
                t.getProject().javaexec(ajc -> {
                    ajc.setClasspath(aspectjBasePlugin.getAspectjConfiguration());
                    ajc.setMain("org.aspectj.tools.ajc.Main");

                    ajc.args("-inpath", abstractCompile.getDestinationDir());
                    ajc.args("-classpath", abstractCompile.getClasspath().getAsPath());

                    if (!aspectpath.isEmpty()) {
                        ajc.args("-aspectpath", aspectpath.getAsPath());
                    }

                    ajc.args("-target", abstractCompile.getTargetCompatibility());
                    ajc.args("-source", abstractCompile.getSourceCompatibility());

                    ajc.args("-d", abstractCompile.getDestinationDir());

                    CompileOptions options = null;
                    if (abstractCompile instanceof JavaCompile) {
                        options = ((JavaCompile) abstractCompile).getOptions();
                    } else if (abstractCompile instanceof GroovyCompile) {
                        options = ((GroovyCompile) abstractCompile).getOptions();
                    } else if (abstractCompile instanceof AbstractScalaCompile) {
                        options = ((AbstractScalaCompile) abstractCompile).getOptions();
                    }

                    if (options != null) {


                        FileCollection bootstrapClasspath = options.getBootstrapClasspath();
                        if (bootstrapClasspath != null && !bootstrapClasspath.isEmpty()) {
                            ajc.args("-bootclasspath", bootstrapClasspath.getAsPath());
                        }
                        String encoding = options.getEncoding();
                        if (encoding != null) {
                            ajc.args("-encoding", encoding);
                        }

                        if (options.isVerbose()) {
                            ajc.args("-verbose");
                        }

                        if (options.isDeprecation()) {
                            ajc.args("-deprecation");
                        }

                        if (options.isDebug()) {
                            String debugLevel = options.getDebugOptions().getDebugLevel();
                            if (debugLevel != null) {
                                ajc.args("-g:" + debugLevel);
                            }
                        } else {
                            ajc.args("-g:none");
                        }
                    }
                    ajc.args(ajcArgs);
                });
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
}
