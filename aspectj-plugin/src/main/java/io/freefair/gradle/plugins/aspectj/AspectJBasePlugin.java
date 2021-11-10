package io.freefair.gradle.plugins.aspectj;

import io.freefair.gradle.plugins.aspectj.internal.AspectjRuntime;
import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.annotation.Nonnull;

@Getter
public class AspectJBasePlugin implements Plugin<Project> {

    private AspectjRuntime aspectjRuntime;

    @Override
    public void apply(@Nonnull Project project) {
        aspectjRuntime = new AspectjRuntime(project);

        project.afterEvaluate(p -> {
            project.getTasks().withType(AspectjCompile.class).configureEach(aspectjCompile -> {
                aspectjCompile.getAspectjClasspath().from(aspectjRuntime.inferAspectjClasspath(aspectjCompile.getClasspath()));
            });
        });
    }
}
