package io.freefair.gradle.plugins.plantuml;


import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.jvm.internal.JvmPluginServices;

import javax.inject.Inject;

/**
 * @author Lars Grefer
 */
public abstract class PlantumlPlugin implements Plugin<Project> {

    @Inject
    public abstract JvmPluginServices getJvmPluginServices();

    @Override
    public void apply(Project project) {
        Configuration plantuml = project.getConfigurations().create("plantuml");

        getJvmPluginServices().configureAsRuntimeClasspath(plantuml);

        plantuml.defaultDependencies(s -> {
            // Note that this version should be kept in sync with build.gradle
            s.add(project.getDependencies().create("net.sourceforge.plantuml:plantuml:1.2025.10"));
        });

        project.getTasks().withType(PlantumlTask.class).configureEach(plantumlTask -> {
            plantumlTask.getPlantumlClasspath().from(plantuml);
            plantumlTask.getOutputDirectory().convention(project.getLayout().getBuildDirectory().dir("plantuml"));
        });

        project.getTasks().register("plantUml", PlantumlTask.class, plantumlTask -> {
            plantumlTask.source("src/plantuml");
        });
    }
}
