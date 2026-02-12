package io.freefair.gradle.plugins.jacoco;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.reporting.DirectoryReport;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;
import org.gradle.testing.jacoco.tasks.JacocoReport;

/**
 * @author Lars Grefer
 */
public class AggregateJacocoReportPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JacocoPlugin.class);

        project.getTasks().register("aggregateJacocoReport", JacocoReport.class, reportTask -> {

            reportTask.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
            reportTask.setDescription(String.format("Generates aggregated code coverage report for the %s project.", project.getPath()));

            project.allprojects(subproject -> {
                subproject.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
                    SourceSetContainer sourceSets = subproject.getExtensions().getByType(JavaPluginExtension.class).getSourceSets();
                    SourceSet main = sourceSets.named(SourceSet.MAIN_SOURCE_SET_NAME).get();
                    reportTask.sourceSets(main);
                });

                subproject.getTasks()
                        .withType(Test.class)
                        .forEach(reportTask::executionData);
            });

            JacocoPluginExtension reportingExtension = project.getExtensions().getByType(JacocoPluginExtension.class);
            reportTask.getReports().getHtml().getRequired().set(true);
            reportTask.getReports().all(report -> {
                if (report instanceof DirectoryReport) {
                    ((DirectoryReport) report).getOutputLocation().convention(reportingExtension
                            .getReportsDirectory()
                            .dir(reportTask.getName() + "/" + report.getName()));
                }
                else if (report instanceof SingleFileReport) {
                    ((SingleFileReport) report).getOutputLocation().convention(reportingExtension
                            .getReportsDirectory()
                            .file(reportTask.getName() + "/" + reportTask.getName() + "." + report.getName()));
                }
            });
        });


    }
}
