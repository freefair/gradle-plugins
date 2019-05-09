package io.freefair.gradle.plugins.jacoco;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.reporting.Report;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;
import org.gradle.testing.jacoco.tasks.JacocoReport;

import java.io.File;

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
                    SourceSetContainer sourceSets = subproject.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();
                    SourceSet main = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
                    reportTask.sourceSets(main);
                });

                subproject.getTasks()
                        .withType(Test.class)
                        .forEach(reportTask::executionData);
            });

            JacocoPluginExtension reportingExtension = project.getExtensions().getByType(JacocoPluginExtension.class);
            reportTask.getReports().getHtml().setEnabled(true);
            reportTask.getReports().all(report -> {
                if (report.getOutputType().equals(Report.OutputType.DIRECTORY)) {
                    report.setDestination(project.provider(() -> new File(reportingExtension.getReportsDir(), reportTask.getName() + "/" + report.getName())));
                }
                else {
                    report.setDestination(project.provider(() -> new File(reportingExtension.getReportsDir(), reportTask.getName() + "/" + reportTask.getName() + "." + report.getName())));
                }
            });
        });


    }
}
