package io.freefair.gradle.plugins.aspectj;

import io.freefair.gradle.plugins.aspectj.internal.DefaultAspectjSourceDirectorySet;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.tasks.DefaultSourceSet;
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

        project.getPlugins().withId("com.android.application", ignored -> {
            throw new IllegalStateException("The 'io.freefair.aspectj' plugin is not compatible with android projects");
        });
        project.getPlugins().withId("com.android.library", ignored -> {
            throw new IllegalStateException("The 'io.freefair.aspectj' plugin is not compatible with android projects");
        });

        this.project = project;
        project.getPlugins().apply(AspectJBasePlugin.class);
        project.getPlugins().apply(JavaBasePlugin.class);

        JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);

        JavaToolchainService service = project.getExtensions().getByType(JavaToolchainService.class);
        defaultLauncher = service.launcherFor(javaExtension.getToolchain());

        javaExtension.getSourceSets().all(this::configureSourceSet);

        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {

            SourceSet main = javaExtension.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
            SourceSet test = javaExtension.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);

            Configuration aspectpath = project.getConfigurations().getByName(WeavingSourceSet.getAspectConfigurationName(main));
            Configuration testAspectpath = project.getConfigurations().getByName(WeavingSourceSet.getAspectConfigurationName(test));

            testAspectpath.extendsFrom(aspectpath);

            WeavingSourceSet.getAspectPath(test).setFrom(main.getOutput(), testAspectpath);
        });
    }

    private void configureSourceSet(SourceSet sourceSet) {
        sourceSet.getExtensions().add(WeavingSourceSet.IN_PATH_EXTENSION_NAME, project.getObjects().fileCollection());
        sourceSet.getExtensions().add(WeavingSourceSet.ASPECT_PATH_EXTENSION_NAME, project.getObjects().fileCollection());

        AspectjSourceDirectorySet aspectj = getAspectjSourceDirectorySet(sourceSet);
        sourceSet.getExtensions().add(AspectjSourceDirectorySet.class, "aspectj", aspectj);

        final SourceDirectorySet aspectjSource = AspectjSourceSet.getAspectj(sourceSet);
        aspectjSource.srcDir("src/" + sourceSet.getName() + "/aspectj");

        // Explicitly capture only a FileCollection in the lambda below for compatibility with configuration-cache.
        //noinspection UnnecessaryLocalVariable
        FileCollection aspectjSourceFileCollection = aspectjSource;
        sourceSet.getResources().getFilter().exclude(element -> aspectjSourceFileCollection.contains(element.getFile()));
        sourceSet.getAllJava().source(aspectjSource);
        sourceSet.getAllSource().source(aspectjSource);

        Configuration aspect = project.getConfigurations().create(WeavingSourceSet.getAspectConfigurationName(sourceSet));
        WeavingSourceSet.getAspectPath(sourceSet).from(aspect);

        Configuration inpath = project.getConfigurations().create(WeavingSourceSet.getInpathConfigurationName(sourceSet));
        WeavingSourceSet.getInPath(sourceSet).from(inpath);

        project.getConfigurations().getByName(sourceSet.getImplementationConfigurationName()).extendsFrom(aspect);

        project.getConfigurations().getByName(sourceSet.getCompileOnlyConfigurationName()).extendsFrom(inpath);

        final TaskProvider<AspectjCompile> compileTask = project.getTasks().register(sourceSet.getCompileTaskName("aspectj"), AspectjCompile.class, compile -> {
            JvmPluginsHelper.compileAgainstJavaOutputs(compile, sourceSet, project.getObjects());
            JvmPluginsHelper.configureAnnotationProcessorPath(sourceSet, aspectjSource, compile.getOptions(), project);
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

    private AspectjSourceDirectorySet getAspectjSourceDirectorySet(SourceSet sourceSet) {
        String name = sourceSet.getName();
        String displayName = ((DefaultSourceSet)sourceSet).getDisplayName();

        AspectjSourceDirectorySet aspectjSourceDirectorySet = project.getObjects().newInstance(DefaultAspectjSourceDirectorySet.class, project.getObjects().sourceDirectorySet(name, displayName + " Groovy source"));
        aspectjSourceDirectorySet.getFilter().include("**/*.java", "**/*.aj");
        return aspectjSourceDirectorySet;
    }
}
