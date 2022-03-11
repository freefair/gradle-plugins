package io.freefair.gradle.plugins.aspectj;

import io.freefair.gradle.plugins.aspectj.internal.AspectJRuntime;
import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;

import javax.annotation.Nonnull;

/**
 * @see org.gradle.api.plugins.GroovyBasePlugin
 * @author Lars Grefer
 */
@Getter
public class AspectJBasePlugin implements Plugin<Project> {

    private AspectJRuntime aspectjRuntime;

    @Override
    public void apply(@Nonnull Project project) {
        aspectjRuntime = new AspectJRuntime(project);

        project.afterEvaluate(p -> {
            project.getTasks().withType(AspectjCompile.class).configureEach(aspectjCompile -> {
                if (aspectjCompile.getAspectjClasspath().isEmpty()) {
                    ConfigurableFileCollection searchPath = project.files(aspectjCompile.getClasspath(), aspectjCompile.getAjcOptions().getAspectpath());
                    aspectjCompile.getAspectjClasspath().from(aspectjRuntime.inferAspectjClasspath(searchPath));
                }
            });
        });
    }
}
