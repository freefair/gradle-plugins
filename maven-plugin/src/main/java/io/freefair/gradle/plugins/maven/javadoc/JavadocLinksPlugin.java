package io.freefair.gradle.plugins.maven.javadoc;

import io.freefair.gradle.plugins.okhttp.OkHttpPlugin;
import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.MinimalJavadocOptions;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import java.io.File;

@Getter
public class JavadocLinksPlugin implements Plugin<Project> {

    private Project project;
    private OkHttpPlugin okHttpPlugin;

    @Override
    public void apply(Project project) {
        this.project = project;

        okHttpPlugin = project.getPlugins().apply(OkHttpPlugin.class);

        project.getPlugins().withType(AggregateJavadocPlugin.class, ajp -> {

            project.afterEvaluate(p -> {
                this.addLinks(ajp.getJavadocTask(), ajp.getJavadocClasspath());
            });
        });

        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
            TaskProvider<Javadoc> javadoc = project.getTasks().named(JavaPlugin.JAVADOC_TASK_NAME, Javadoc.class);

            SourceSet main = project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets().getByName("main");
            Configuration compileClasspath = project.getConfigurations().getByName(main.getCompileClasspathConfigurationName());

            project.afterEvaluate(p -> {
                this.addLinks(javadoc, compileClasspath);
            });
        });
    }

    public void addLinks(TaskProvider<Javadoc> javadocTaskProvider, Configuration classpath) {

        TaskProvider<ResolveJavadocLinks> resolveJavadocLinks = project.getTasks().register("resolveJavadocLinks", ResolveJavadocLinks.class, okHttpPlugin.getOkHttpClient());

        resolveJavadocLinks.configure(rjd -> {
            rjd.getJavadocTool().convention(javadocTaskProvider.get().getJavadocTool());
            rjd.setClasspath(classpath);

            File destinationDir = rjd.getTemporaryDir();
            rjd.getOutputFile().set(new File(destinationDir, "links.txt"));
        });

        javadocTaskProvider.configure(javadoc -> {
            javadoc.dependsOn(resolveJavadocLinks);
            javadoc.getInputs()
                    .file(resolveJavadocLinks.map(ResolveJavadocLinks::getOutputFile))
                    .withPathSensitivity(PathSensitivity.NAME_ONLY);

            MinimalJavadocOptions options = javadoc.getOptions();

            if (options instanceof StandardJavadocDocletOptions) {
                StandardJavadocDocletOptions standardOptions = (StandardJavadocDocletOptions) options;
                standardOptions.linksFile(resolveJavadocLinks.get().getOutputFile().get().getAsFile());
            }
        });

    }

}
