package io.freefair.gradle.plugins.javadoc;

import io.freefair.gradle.plugins.AbstractPlugin;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.file.CopySpec;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import java.io.File;

/**
 * @author Lars Grefer
 */
@SuppressWarnings("unused")
@Getter
public class JavadocIoPlugin extends AbstractPlugin {

    private Configuration javadocIoConfiguration;

    private Copy extractJavadocIo;
    private Task configureExtractJavadocIo;

    @Override
    public void apply(final Project project) {
        super.apply(project);

        javadocIoConfiguration = project.getConfigurations().create("javadocIo");

        extractJavadocIo = project.getTasks().create("extractJavadocIo", Copy.class);
        extractJavadocIo.setDestinationDir(new File(project.getBuildDir(), "exploded-javadoc"));
        extractJavadocIo.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
        extractJavadocIo.setDescription("Extract all javadocs, so the javadoc-tasks can link against them");


        project.getTasks().withType(Javadoc.class, new Action<Javadoc>() {
            @Override
            public void execute(Javadoc javadoc) {
                javadoc.dependsOn(extractJavadocIo);
                javadoc.getInputs().dir(extractJavadocIo.getDestinationDir());
            }
        });

        configureExtractJavadocIo = project.getTasks().create("configureExtractJavadocIo");
        configureExtractJavadocIo.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);

        extractJavadocIo.dependsOn(configureExtractJavadocIo);

        configureExtractJavadocIo.doFirst(new Action<Task>() {
            @Override
            public void execute(Task configureExtractJavadocIo) {
                for (ResolvedDependency resolvedDependency : javadocIoConfiguration.getResolvedConfiguration().getFirstLevelModuleDependencies()) {

                    final String moduleGroup = resolvedDependency.getModuleGroup();
                    final String moduleName = resolvedDependency.getModuleName();
                    final String moduleVersion = resolvedDependency.getModuleVersion();

                    String dirName = moduleGroup + File.separatorChar + moduleName + File.separatorChar + moduleVersion;

                    final File zipFile = resolvedDependency.getModuleArtifacts().iterator().next().getFile();

                    extractJavadocIo.into(dirName, new Action<CopySpec>() {
                        @Override
                        public void execute(CopySpec copySpec) {
                            copySpec.from(project.zipTree(zipFile));
                        }
                    });

                    final File dir = new File(extractJavadocIo.getDestinationDir(), dirName);

                    project.getTasks().withType(Javadoc.class, new Action<Javadoc>() {
                        @Override
                        public void execute(Javadoc javadoc) {
                            StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) javadoc.getOptions();

                            options.linksOffline(
                                    "http://static.javadoc.io/" + moduleGroup + "/" + moduleName + "/" + moduleVersion,
                                    dir.getAbsolutePath()
                            );
                        }
                    });

                }
            }
        });
    }
}
