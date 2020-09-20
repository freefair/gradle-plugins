package io.freefair.gradle.plugins.aspectj;

import io.freefair.gradle.plugins.aspectj.internal.DefaultAspectjSourceSet;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.internal.JvmPluginsHelper;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;

/**
 * @see org.gradle.api.plugins.GroovyBasePlugin
 * @see org.gradle.api.plugins.GroovyPlugin
 */
public class AspectJPlugin implements Plugin<Project> {

    private Project project;

    @Override
    public void apply(Project project) {
        if (project.getPlugins().hasPlugin(AspectJPostCompileWeavingPlugin.class)) {
            throw new IllegalStateException("Another aspectj plugin (which is excludes this one) has already been applied to the project.");
        }

        this.project = project;
        project.getPlugins().apply(AspectJBasePlugin.class);
        project.getPlugins().apply(JavaBasePlugin.class);

        JavaPluginConvention plugin = project.getConvention().getPlugin(JavaPluginConvention.class);

        plugin.getSourceSets().all(this::configureSourceSet);

        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {

            SourceSet main = plugin.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
            SourceSet test = plugin.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);

            DefaultAspectjSourceSet mainAj = new DslObject(main).getConvention().getPlugin(DefaultAspectjSourceSet.class);
            DefaultAspectjSourceSet testAj = new DslObject(test).getConvention().getPlugin(DefaultAspectjSourceSet.class);

            Configuration aspectpath = project.getConfigurations().getByName(mainAj.getAspectConfigurationName());
            Configuration testAspectpath = project.getConfigurations().getByName(testAj.getAspectConfigurationName());

            testAspectpath.extendsFrom(aspectpath);

            testAj.setAspectPath(project.getObjects().fileCollection().from(main.getOutput(), testAspectpath));
        });
    }

    private void configureSourceSet(SourceSet sourceSet) {
        DefaultAspectjSourceSet aspectjSourceSet = new DefaultAspectjSourceSet(project.getObjects(), sourceSet);
        new DslObject(sourceSet).getConvention().getPlugins().put("aspectj", aspectjSourceSet);

        aspectjSourceSet.getAspectj().srcDir("src/" + sourceSet.getName() + "/aspectj");
        sourceSet.getResources().getFilter().exclude(element -> aspectjSourceSet.getAspectj().contains(element.getFile()));
        sourceSet.getAllJava().source(aspectjSourceSet.getAspectj());
        sourceSet.getAllSource().source(aspectjSourceSet.getAspectj());

        Configuration aspect = project.getConfigurations().create(aspectjSourceSet.getAspectConfigurationName());
        aspectjSourceSet.setAspectPath(aspect);

        Configuration inpath = project.getConfigurations().create(aspectjSourceSet.getInpathConfigurationName());
        aspectjSourceSet.setInPath(inpath);

        project.getConfigurations().getByName(sourceSet.getImplementationConfigurationName()).extendsFrom(aspect);

        project.getConfigurations().getByName(sourceSet.getCompileOnlyConfigurationName()).extendsFrom(inpath);

        final Provider<AspectjCompile> compileTask = project.getTasks().register(sourceSet.getCompileTaskName("aspectj"), AspectjCompile.class, compile -> {
            JvmPluginsHelper.configureForSourceSet(sourceSet, aspectjSourceSet.getAspectj(), compile, compile.getOptions(), project);
            compile.dependsOn(sourceSet.getCompileJavaTaskName());
            compile.setDescription("Compiles the " + sourceSet.getName() + " AspectJ source.");
            compile.setSource(aspectjSourceSet.getAspectj());
            compile.getAjcOptions().getAspectpath().from(aspectjSourceSet.getAspectPath());
            compile.getAjcOptions().getInpath().from(aspectjSourceSet.getInPath());
        });
        JvmPluginsHelper.configureOutputDirectoryForSourceSet(sourceSet, aspectjSourceSet.getAspectj(), project, compileTask, compileTask.map(AspectjCompile::getOptions));

        project.getTasks().named(sourceSet.getClassesTaskName(), task -> task.dependsOn(compileTask));
    }
}
