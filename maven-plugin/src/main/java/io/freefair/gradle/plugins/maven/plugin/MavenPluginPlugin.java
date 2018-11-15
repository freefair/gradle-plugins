package io.freefair.gradle.plugins.maven.plugin;

import io.freefair.gradle.plugins.maven.MavenPublishJavaPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.publish.maven.tasks.GenerateMavenPom;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.language.jvm.tasks.ProcessResources;

/**
 * A Gradle plugin for building Maven plugins.
 *
 * @author Lars Grefer
 */
public class MavenPluginPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);
        MavenPublishJavaPlugin mavenPublishJavaPlugin = project.getPlugins().apply(MavenPublishJavaPlugin.class);

        mavenPublishJavaPlugin.getPublication().getPom().setPackaging("maven-plugin");

        GenerateMavenPom generateMavenPom = (GenerateMavenPom) project.getTasks().getByName("generatePomFileForMavenJavaPublication");

        DescriptorGeneratorTask generateMavenPluginDescriptor = project.getTasks().create("generateMavenPluginDescriptor", DescriptorGeneratorTask.class);

        generateMavenPluginDescriptor.dependsOn(generateMavenPom);
        generateMavenPluginDescriptor.getPomFile().set(generateMavenPom.getDestination());

        generateMavenPluginDescriptor.getOutputDirectory().set(
                project.getLayout().getBuildDirectory().dir("maven-plugin")
        );

        SourceSet main = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().getByName("main");
        generateMavenPluginDescriptor.getSourceDirectories().from(main.getAllJava().getSourceDirectories());
        JavaCompile javaCompile = (JavaCompile) project.getTasks().getByName(main.getCompileJavaTaskName());
        generateMavenPluginDescriptor.getClassesDirectories().from(javaCompile);

        ProcessResources processResources = (ProcessResources) project.getTasks().getByName(JavaPlugin.PROCESS_RESOURCES_TASK_NAME);
        processResources.from(generateMavenPluginDescriptor);

    }
}
