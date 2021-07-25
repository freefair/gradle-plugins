package io.freefair.gradle.plugins.aspectj;

import io.freefair.gradle.plugins.aspectj.internal.DefaultWeavingSourceSet;
import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.internal.tasks.compile.HasCompileOptions;
import org.gradle.api.plugins.GroovyPlugin;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.scala.ScalaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.AbstractCompile;

@NonNullApi
public class AspectJPostCompileWeavingPlugin implements Plugin<Project> {

    private Project project;
    private AspectJBasePlugin aspectjBasePlugin;
    private SourceSetContainer sourceSets;

    @Override
    public void apply(Project project) {
        if (project.getPlugins().hasPlugin(AspectJPlugin.class)) {
            throw new IllegalStateException("Another aspectj plugin (which is excludes this one) has already been applied to the project.");
        }

        this.project = project;
        aspectjBasePlugin = project.getPlugins().apply(AspectJBasePlugin.class);

        project.getPlugins().apply(JavaBasePlugin.class);
        sourceSets = project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets();

        sourceSets.all(this::configureSourceSetDefaults);

        project.getPlugins().withType(JavaPlugin.class, plugin -> this.configurePlugin("java"));
        project.getPlugins().withType(GroovyPlugin.class, plugin -> this.configurePlugin("groovy"));
        project.getPlugins().withType(ScalaPlugin.class, plugin -> this.configurePlugin("scala"));
        project.getPlugins().withId("org.jetbrains.kotlin.jvm", plugin -> this.configurePlugin("kotlin"));
    }

    private void configureSourceSetDefaults(SourceSet sourceSet) {
        project.afterEvaluate(p ->
                p.getDependencies().add(sourceSet.getImplementationConfigurationName(), "org.aspectj:aspectjrt:" + aspectjBasePlugin.getAspectjExtension().getVersion().get())
        );

        DefaultWeavingSourceSet weavingSourceSet = new DefaultWeavingSourceSet(sourceSet);
        new DslObject(sourceSet).getConvention().add("aspectj", weavingSourceSet);

        Configuration aspectpath = project.getConfigurations().create(weavingSourceSet.getAspectConfigurationName());
        weavingSourceSet.setAspectPath(aspectpath);

        Configuration inpath = project.getConfigurations().create(weavingSourceSet.getInpathConfigurationName());
        weavingSourceSet.setInPath(inpath);

        project.getConfigurations().getByName(sourceSet.getImplementationConfigurationName()).extendsFrom(aspectpath);
        project.getConfigurations().getByName(sourceSet.getCompileOnlyConfigurationName()).extendsFrom(inpath);
    }

    private void configurePlugin(String language) {
        sourceSets.all(sourceSet -> {
            WeavingSourceSet weavingSourceSet = new DslObject(sourceSet).getConvention().getByType(WeavingSourceSet.class);

            FileCollection aspectpath = weavingSourceSet.getAspectPath();
            FileCollection inpath = weavingSourceSet.getInPath();

            project.getTasks().named(sourceSet.getCompileTaskName(language), AbstractCompile.class, compileTask -> {
                AjcAction ajcAction = enhanceWithWeavingAction(compileTask, aspectpath, inpath, aspectjBasePlugin.getAspectjConfiguration());
                if (compileTask instanceof HasCompileOptions) {
                    HasCompileOptions compileTaskWithOptions = (HasCompileOptions) compileTask;
                    ajcAction.getOptions().getBootclasspath().from(compileTaskWithOptions.getOptions().getBootstrapClasspath());
                    ajcAction.getOptions().getExtdirs().from(compileTaskWithOptions.getOptions().getExtensionDirs());
                }
            });
        });
    }

    private AjcAction enhanceWithWeavingAction(AbstractCompile abstractCompile, FileCollection aspectpath, FileCollection inpath, Configuration aspectjConfiguration) {
        AjcAction action = project.getObjects().newInstance(AjcAction.class);

        action.getOptions().getAspectpath().from(aspectpath);
        action.getOptions().getInpath().from(inpath);
        action.getAdditionalInpath().from(abstractCompile.getDestinationDirectory());
        action.getClasspath().from(aspectjConfiguration);

        action.addToTask(abstractCompile);

        return action;
    }

}
