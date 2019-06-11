package io.freefair.gradle.plugins.aspectj;

import io.freefair.gradle.plugins.aspectj.internal.DefaultAspectjSourceSet;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.internal.SourceSetUtil;
import org.gradle.api.provider.Provider;

/**
 * @see org.gradle.api.plugins.GroovyBasePlugin
 * @see org.gradle.api.plugins.GroovyPlugin
 */
public class AspectJPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(AspectJBasePlugin.class);
        project.getPlugins().apply(JavaBasePlugin.class);

        JavaPluginConvention plugin = project.getConvention().getPlugin(JavaPluginConvention.class);

        plugin.getSourceSets().all(sourceSet -> {
            DefaultAspectjSourceSet aspectjSourceSet = new DefaultAspectjSourceSet(project.getObjects(), sourceSet);
            new DslObject(sourceSet).getConvention().getPlugins().put("aspectj", aspectjSourceSet);

            aspectjSourceSet.getAspectj().srcDir("src/" + sourceSet.getName() + "/aspectj");
            sourceSet.getResources().getFilter().exclude(element -> aspectjSourceSet.getAspectj().contains(element.getFile()));
            sourceSet.getAllJava().source(aspectjSourceSet.getAspectj());
            sourceSet.getAllSource().source(aspectjSourceSet.getAspectj());

            Configuration aspect = project.getConfigurations().create(aspectjSourceSet.getAspectConfigurationName());
            aspectjSourceSet.setAspectPath(aspect);

            final Provider<AspectjCompile> compileTask = project.getTasks().register(sourceSet.getCompileTaskName("aspectj"), AspectjCompile.class, compile -> {
                SourceSetUtil.configureForSourceSet(sourceSet, aspectjSourceSet.getAspectj(), compile, compile.getOptions(), project);
                compile.dependsOn(sourceSet.getCompileJavaTaskName());
                compile.setDescription("Compiles the " + sourceSet.getName() + " AspectJ source.");
                compile.setSource(aspectjSourceSet.getAspectj());
                compile.getAjcOptions().getAspectpath().from(aspect);
            });
            SourceSetUtil.configureOutputDirectoryForSourceSet(sourceSet, aspectjSourceSet.getAspectj(), project, compileTask, compileTask.map(AspectjCompile::getOptions));


            project.getTasks().named(sourceSet.getClassesTaskName(), task -> task.dependsOn(compileTask));
        });
    }
}
