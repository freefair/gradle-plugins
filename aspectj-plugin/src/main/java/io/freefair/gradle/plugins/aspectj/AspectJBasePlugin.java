package io.freefair.gradle.plugins.aspectj;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPluginConvention;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

@Getter
public class AspectJBasePlugin implements Plugin<Project> {

    private Configuration aspectjConfiguration;
    private AspectJExtension aspectjExtension;

    @Override
    public void apply(@Nonnull Project project) {
        aspectjExtension = project.getExtensions().create("aspectj", AspectJExtension.class);

        aspectjConfiguration = project.getConfigurations().create("aspectj");

        aspectjConfiguration.defaultDependencies(dependencies -> {
            dependencies.add(project.getDependencies().create("org.aspectj:aspectjtools:" + aspectjExtension.getVersion().get()));
        });

        project.getTasks().withType(AspectjCompile.class).configureEach(aspectjCompile -> {
            aspectjCompile.getAspectjClasspath().from(aspectjConfiguration);
        });
    }
}
