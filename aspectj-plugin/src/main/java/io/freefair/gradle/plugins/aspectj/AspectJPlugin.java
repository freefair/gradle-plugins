package io.freefair.gradle.plugins.aspectj;

import io.freefair.gradle.plugins.aspectj.internal.DefaultAspectjSourceSet;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.internal.JvmPluginsHelper;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;

/**
 * @see org.gradle.api.plugins.GroovyBasePlugin
 * @see org.gradle.api.plugins.GroovyPlugin
 */
public class AspectJPlugin implements Plugin<Project> {

    private Project project;

    private Provider<JavaLauncher> defaultLauncher;

    @Override
    public void apply(Project project) {
        if (project.getPlugins().hasPlugin(AspectJPostCompileWeavingPlugin.class)) {
            throw new IllegalStateException("Another aspectj plugin (which is excludes this one) has already been applied to the project.");
        }

        this.project = project;
        project.getPlugins().apply(AspectJBasePlugin.class);
        project.getPlugins().apply(JavaBasePlugin.class);

        JavaPluginExtension plugin = project.getExtensions().getByType(JavaPluginExtension.class);

        plugin.getSourceSets().all(this::configureSourceSet);

        JavaToolchainService service = project.getExtensions().getByType(JavaToolchainService.class);
        defaultLauncher = service.launcherFor(plugin.getToolchain());

        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {

            SourceSet main = plugin.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
            SourceSet test = plugin.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);

            Configuration aspectpath = project.getConfigurations().getByName(WeavingSourceSet.getAspectConfigurationName(main));
            Configuration testAspectpath = project.getConfigurations().getByName(WeavingSourceSet.getAspectConfigurationName(test));

            testAspectpath.extendsFrom(aspectpath);

            WeavingSourceSet.getAspectPath(test).setFrom(main.getOutput(), testAspectpath);
        });
    }

    private void configureSourceSet(SourceSet sourceSet) {
        DefaultAspectjSourceSet aspectjSourceSet = new DefaultAspectjSourceSet(project.getObjects(), sourceSet);
        new DslObject(sourceSet).getConvention().getPlugins().put("aspectj", aspectjSourceSet);

        final SourceDirectorySet aspectjSource = AspectjSourceSet.getAspectj(sourceSet);
        aspectjSource.srcDir("src/" + sourceSet.getName() + "/aspectj");

        sourceSet.getResources().getFilter().exclude(element -> AspectjSourceSet.getAspectj(sourceSet).contains(element.getFile()));
        sourceSet.getAllJava().source(aspectjSource);
        sourceSet.getAllSource().source(aspectjSource);

        Configuration aspect = project.getConfigurations().create(WeavingSourceSet.getAspectConfigurationName(sourceSet));
        WeavingSourceSet.getAspectPath(sourceSet).from(aspect);

        Configuration inpath = project.getConfigurations().create(WeavingSourceSet.getInpathConfigurationName(sourceSet));
        WeavingSourceSet.getInPath(sourceSet).from(inpath);

        project.getConfigurations().getByName(sourceSet.getImplementationConfigurationName()).extendsFrom(aspect);

        project.getConfigurations().getByName(sourceSet.getCompileOnlyConfigurationName()).extendsFrom(inpath);

        final TaskProvider<AspectjCompile> compileTask = project.getTasks().register(sourceSet.getCompileTaskName("aspectj"), AspectjCompile.class, compile -> {
            JvmPluginsHelper.configureForSourceSet(sourceSet, aspectjSource, compile, compile.getOptions(), project);
            compile.dependsOn(sourceSet.getCompileJavaTaskName());
            compile.getLauncher().convention(defaultLauncher);
            compile.setDescription("Compiles the " + sourceSet.getName() + " AspectJ source.");
            compile.setSource(aspectjSource);
            compile.getAjcOptions().getAspectpath().from(WeavingSourceSet.getAspectPath(sourceSet));
            compile.getAjcOptions().getInpath().from(WeavingSourceSet.getInPath(sourceSet));
        });
        JvmPluginsHelper.configureOutputDirectoryForSourceSet(sourceSet, aspectjSource, project, compileTask, compileTask.map(AspectjCompile::getOptions));

        project.getTasks().named(sourceSet.getClassesTaskName(), task -> task.dependsOn(compileTask));
    }
}
