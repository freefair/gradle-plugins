package io.freefair.gradle.plugins.aspectj;

import io.freefair.gradle.plugins.aspectj.internal.AspectJUtil;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.tasks.compile.HasCompileOptions;
import org.gradle.api.plugins.GroovyPlugin;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.scala.ScalaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile;

public class AspectJPostCompileWeavingPlugin implements Plugin<Project> {

    private Project project;
    private AspectJBasePlugin aspectjBasePlugin;
    private SourceSetContainer sourceSets;
    private Provider<JavaLauncher> defaultLauncher;

    @Override
    public void apply(Project project) {
        if (project.getPlugins().hasPlugin(AspectJPlugin.class)) {
            throw new IllegalStateException("Another aspectj plugin (which is excludes this one) has already been applied to the project.");
        }

        project.getPlugins().withId("com.android.application", ignored -> {
            throw new IllegalStateException("The 'io.freefair.aspectj.post-compile-weaving' plugin is not compatible with android projects");
        });
        project.getPlugins().withId("com.android.library", ignored -> {
            throw new IllegalStateException("The 'io.freefair.aspectj.post-compile-weaving' plugin is not compatible with android projects");
        });

        this.project = project;
        aspectjBasePlugin = project.getPlugins().apply(AspectJBasePlugin.class);

        project.getPlugins().apply(JavaBasePlugin.class);
        JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        sourceSets = javaPluginExtension.getSourceSets();

        JavaToolchainService service = project.getExtensions().getByType(JavaToolchainService.class);
        defaultLauncher = service.launcherFor(javaPluginExtension.getToolchain());

        sourceSets.all(this::configureSourceSetDefaults);

        project.getPlugins().withType(JavaPlugin.class, plugin -> this.configurePlugin("java"));
        project.getPlugins().withType(GroovyPlugin.class, plugin -> this.configurePlugin("groovy"));
        project.getPlugins().withType(ScalaPlugin.class, plugin -> this.configurePlugin("scala"));
        project.getPlugins().withId("org.jetbrains.kotlin.jvm", plugin -> this.configurePlugin("kotlin"));
    }

    private void configureSourceSetDefaults(SourceSet sourceSet) {
        sourceSet.getExtensions().add(WeavingSourceSet.IN_PATH_EXTENSION_NAME, project.getObjects().fileCollection());
        sourceSet.getExtensions().add(WeavingSourceSet.ASPECT_PATH_EXTENSION_NAME, project.getObjects().fileCollection());

        Configuration aspectpath = project.getConfigurations().create(WeavingSourceSet.getAspectConfigurationName(sourceSet));
        aspectpath.exclude(AspectJUtil.getAspectJToolsExclude());
        WeavingSourceSet.getAspectPath(sourceSet).from(aspectpath);

        Configuration inpath = project.getConfigurations().create(WeavingSourceSet.getInpathConfigurationName(sourceSet));
        WeavingSourceSet.getInPath(sourceSet).from(inpath);

        project.getConfigurations().getByName(sourceSet.getImplementationConfigurationName()).extendsFrom(aspectpath);
        project.getConfigurations().getByName(sourceSet.getCompileOnlyConfigurationName()).extendsFrom(inpath);
    }

    private void configurePlugin(String language) {
        sourceSets.all(sourceSet -> {
            FileCollection aspectpath = WeavingSourceSet.getAspectPath(sourceSet);
            FileCollection inpath = WeavingSourceSet.getInPath(sourceSet);

            Configuration runtimeClasspath = project.getConfigurations().getByName(sourceSet.getRuntimeClasspathConfigurationName());
            Configuration compileClasspath = project.getConfigurations().getByName(sourceSet.getCompileClasspathConfigurationName());
            ConfigurableFileCollection searchPath = project.files(runtimeClasspath, compileClasspath);
            FileCollection aspectjClasspath = aspectjBasePlugin.getAspectjRuntime().inferAspectjClasspath(searchPath);

            project.getTasks().named(sourceSet.getCompileTaskName(language), compileTask -> {
                AjcAction ajcAction = enhanceWithWeavingAction(compileTask, aspectpath, inpath, aspectjClasspath);
                if (compileTask instanceof HasCompileOptions) {
                    HasCompileOptions compileTaskWithOptions = (HasCompileOptions) compileTask;
                    ajcAction.getOptions().getBootclasspath().from(compileTaskWithOptions.getOptions().getBootstrapClasspath());
                    ajcAction.getOptions().getExtdirs().from(compileTaskWithOptions.getOptions().getExtensionDirs());
                }
            });
        });
    }

    private AjcAction enhanceWithWeavingAction(Task compileTask, FileCollection aspectpath, FileCollection inpath, FileCollection aspectjConfiguration) {
        AjcAction action = project.getObjects().newInstance(AjcAction.class);

        action.getLauncher().convention(defaultLauncher);

        action.getOptions().getAspectpath().from(aspectpath);
        action.getOptions().getInpath().from(inpath);
        if (compileTask instanceof AbstractCompile) {
            action.getAdditionalInpath().from(((AbstractCompile) compileTask).getDestinationDirectory());
        }
        else if (compileTask instanceof KotlinJvmCompile) {
            action.getAdditionalInpath().from(((KotlinJvmCompile) compileTask).getDestinationDirectory());
        }
        action.getClasspath().from(aspectjConfiguration);

        action.addToTask(compileTask);

        return action;
    }

}
