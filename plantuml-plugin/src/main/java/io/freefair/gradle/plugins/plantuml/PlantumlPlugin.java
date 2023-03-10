package io.freefair.gradle.plugins.plantuml;


import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

/**
 * @author Lars Grefer
 */
public class PlantumlPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        Configuration plantuml = project.getConfigurations().create("plantuml");

        plantuml.defaultDependencies(s -> {
            s.add(project.getDependencies().create("net.sourceforge.plantuml:plantuml:1.2023.4"));
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
