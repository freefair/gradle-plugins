package io.freefair.gradle.plugins;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPluginConvention;

@Getter
public class AspectJBasePlugin implements Plugin<Project> {

    private Configuration aspectjConfiguration;
    private AspectJExtension aspectjExtension;

    @Override
    public void apply(Project project) {
        aspectjExtension = project.getExtensions().create("aspectj", AspectJExtension.class, project);
        aspectjExtension.getVersion().set("1.9.2");

        aspectjConfiguration = project.getConfigurations().create("aspectj");

        aspectjConfiguration.defaultDependencies(dependencies -> {
            dependencies.add(project.getDependencies().create("org.aspectj:aspectjtools:" + aspectjExtension.getVersion().get()));
        });

        project.getTasks().withType(Ajc.class, ajc -> {
            ajc.getAspectjClasspath().from(aspectjConfiguration);

            ajc.getSource().set(
                    project.provider(() -> project.getConvention().findPlugin(JavaPluginConvention.class))
                            .map(javaPluginConvention -> javaPluginConvention.getSourceCompatibility().toString())
            );
            ajc.getTarget().set(
                    project.provider(() -> project.getConvention().findPlugin(JavaPluginConvention.class))
                            .map(javaPluginConvention -> javaPluginConvention.getTargetCompatibility().toString())
            );
        });
    }
}
