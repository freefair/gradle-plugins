package io.freefair.gradle.plugins.maven.plugin;

import io.freefair.gradle.plugins.maven.MavenPublishJavaPlugin;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.maven.tasks.GenerateMavenPom;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

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

        // https://github.com/gradle/gradle/issues/10555#issue-492150084
        if (project.getGradle().getGradleVersion().matches("5\\.6(\\.[12])?")) {
            mavenPublishJavaPlugin.getPublication().getPom().withXml(xmlProvider ->
                    xmlProvider.asNode().appendNode("packaging", "maven-plugin")
            );
        }
        else {
            mavenPublishJavaPlugin.getPublication().getPom().setPackaging("maven-plugin");
        }

        TaskProvider<GenerateMavenPom> generateMavenPom = project.getTasks().named("generatePomFileForMavenJavaPublication", GenerateMavenPom.class);

        NamedDomainObjectProvider<SourceSet> main = project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets().named("main");

        TaskProvider<DescriptorGeneratorTask> descriptorGeneratorTaskProvider = project.getTasks().register("generateMavenPluginDescriptor", DescriptorGeneratorTask.class, generateMavenPluginDescriptor -> {

            generateMavenPluginDescriptor.dependsOn(generateMavenPom);
            generateMavenPluginDescriptor.getPomFile().fileProvider(generateMavenPom.map(GenerateMavenPom::getDestination));

            generateMavenPluginDescriptor.getOutputDirectory().set(
                    project.getLayout().getBuildDirectory().dir("generated/resources/maven-plugin-descriptor")
            );

            generateMavenPluginDescriptor.getSourceDirectories().from(main.get().getAllJava().getSourceDirectories());
            JavaCompile javaCompile = (JavaCompile) project.getTasks().named(main.get().getCompileJavaTaskName()).get();

            generateMavenPluginDescriptor.getClassesDirectories().from(javaCompile);
            generateMavenPluginDescriptor.getEncoding().convention(javaCompile.getOptions().getEncoding());
        });

        main.configure(m -> m.getResources().srcDir(descriptorGeneratorTaskProvider));
    }
}
