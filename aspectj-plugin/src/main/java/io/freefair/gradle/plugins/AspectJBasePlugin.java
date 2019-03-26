package io.freefair.gradle.plugins;

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

        project.getTasks().withType(Ajc.class).configureEach(ajc -> {
            ajc.getAspectjClasspath().from(aspectjConfiguration);

            Callable<JavaPluginConvention> javaPluginConventionCallable = () -> project.getConvention().findPlugin(JavaPluginConvention.class);
            ajc.getSource().set(
                    project.provider(javaPluginConventionCallable)
                            .map(javaPluginConvention -> javaPluginConvention.getSourceCompatibility().toString())
            );
            ajc.getTarget().set(
                    project.provider(javaPluginConventionCallable)
                            .map(javaPluginConvention -> javaPluginConvention.getTargetCompatibility().toString())
            );
        });
    }
}
