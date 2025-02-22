package io.freefair.gradle.plugins.gwt;

import io.freefair.gradle.plugins.gwt.tasks.AbstractGwtTask;
import io.freefair.gradle.plugins.gwt.tasks.GwtCodeServerTask;
import io.freefair.gradle.plugins.gwt.tasks.GwtCompileTask;
import io.freefair.gradle.plugins.gwt.tasks.GwtDevModeTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.attributes.VerificationType;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;

/**
 * @author Lars Grefer
 */
public class GwtBasePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getPlugins().apply(JavaPlugin.class);

        Configuration gwtDev = project.getConfigurations().create("gwtDev");

        Configuration gwtClasspath = project.getConfigurations().create("gwtClasspath");

        Configuration gwtSources = project.getConfigurations().create("gwtSources");
        gwtSources.extendsFrom(gwtClasspath);

        gwtSources.getAttributes().attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, project.getObjects().named(VerificationType.class, VerificationType.MAIN_SOURCES));
        gwtSources.getAttributes().attribute(DocsType.DOCS_TYPE_ATTRIBUTE, project.getObjects().named(DocsType.class, DocsType.SOURCES));

        GwtExtension gwtExtension = project.getExtensions().create("gwt", GwtExtension.class);

        project.afterEvaluate(p -> {
            gwtDev.defaultDependencies(ds -> {
                ds.add(project.getDependencies().create("org.gwtproject:gwt-dev:" + gwtExtension.getToolVersion().get()));
            });
        });

        project.getTasks().withType(AbstractGwtTask.class, gwtTask -> {
            gwtTask.setGroup("gwt");

            gwtTask.getGwtClasspath().from(gwtDev);
            gwtTask.getGwtClasspath().from(gwtClasspath);
            JavaPluginExtension pluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
            SourceSet main = pluginExtension.getSourceSets().getByName("main");
            gwtTask.getGwtClasspath().from(main.getAllJava().getSourceDirectories());

            gwtTask.getWorkDir().set(gwtTask.getTemporaryDir());

            gwtTask.getModule().convention(gwtExtension.getModules());

            gwtTask.getSourceLevel().convention(project.getTasks().named("compileJava", JavaCompile.class).map(AbstractCompile::getSourceCompatibility));
        });

        TaskProvider<GwtCompileTask> gwtCompileTaskProvider = project.getTasks().register("gwtCompile", GwtCompileTask.class, gwtCompile -> {
            gwtCompile.getWar().convention(project.getLayout().getBuildDirectory().dir("gwt/compile/war"));
            gwtCompile.getDeploy().convention(project.getLayout().getBuildDirectory().dir("gwt/compile/deploy"));
            gwtCompile.getExtra().convention(project.getLayout().getBuildDirectory().dir("gwt/compile/extra"));

            gwtCompile.getGwtClasspath().from(gwtSources);
        });

        TaskProvider<GwtDevModeTask> gwtDevModeTaskProvider = project.getTasks().register("gwtDevMode", GwtDevModeTask.class, gwtDevMode -> {
            gwtDevMode.getWar().convention(project.getLayout().getBuildDirectory().dir("gwt/dev-mode/war"));
            gwtDevMode.getDeploy().convention(project.getLayout().getBuildDirectory().dir("gwt/dev-mode/deploy"));
            gwtDevMode.getExtra().convention(project.getLayout().getBuildDirectory().dir("gwt/dev-mode/extra"));
        });

        project.getTasks().register("gwtCodeServer", GwtCodeServerTask.class, gwtCodeServer -> {
            gwtCodeServer.getLauncherDir().convention(project.getLayout().getBuildDirectory().dir("gwt/code-server"));
        });

    }
}
