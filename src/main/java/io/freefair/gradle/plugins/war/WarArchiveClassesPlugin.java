package io.freefair.gradle.plugins.war;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.War;

import java.io.File;
import java.util.Collections;

public class WarArchiveClassesPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getTasks().withType(War.class, war -> {

            WarArchiveClassesConvention archiveClassesConvention = new WarArchiveClassesConvention();

            war.getConvention().getPlugins().put("archiveClasses", archiveClassesConvention);

            Jar warClassesJar = project.getTasks().create(war.getName() + "ClassesJar", Jar.class);
            warClassesJar.getConventionMapping().map("baseName", war::getBaseName);
            warClassesJar.getConventionMapping().map("appendix", war::getAppendix);
            warClassesJar.getConventionMapping().map("version", war::getVersion);
            warClassesJar.getConventionMapping().map("classifier", war::getClassifier);

            project.afterEvaluate(p -> {

                warClassesJar.setEnabled(archiveClassesConvention.isArchiveClasses());

                if (archiveClassesConvention.isArchiveClasses()) {
                    FileCollection warClasspath = war.getClasspath();

                    warClassesJar.from(warClasspath != null ? warClasspath.filter(File::isDirectory) : Collections.emptyList());

                    war.setClasspath(warClasspath == null ? null : warClasspath.filter(File::isFile).plus(project.files(warClassesJar)));
                }
            });
        });
    }
}
