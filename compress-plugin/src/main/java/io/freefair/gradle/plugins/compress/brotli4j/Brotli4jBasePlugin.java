package io.freefair.gradle.plugins.compress.brotli4j;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyFactory;
import org.gradle.api.plugins.jvm.internal.JvmPluginServices;
import org.gradle.nativeplatform.MachineArchitecture;
import org.gradle.nativeplatform.OperatingSystemFamily;
import org.gradle.nativeplatform.TargetMachine;
import org.gradle.nativeplatform.internal.DefaultTargetMachineFactory;

import javax.inject.Inject;

@Getter
public abstract class Brotli4jBasePlugin implements Plugin<Project> {

    @Inject
    protected abstract DependencyFactory getDependencyFactory();

    @Inject
    protected abstract JvmPluginServices getJvmPluginServices();

    @Override
    public void apply(Project project) {

        Brotli4jExtension extension = project.getExtensions().create("brotli4j", Brotli4jExtension.class);

        TargetMachine host = new DefaultTargetMachineFactory(project.getObjects()).host();

        Configuration brotli4jConfiguration = project.getConfigurations().create("brotli4j");
        brotli4jConfiguration.setCanBeResolved(true);
        brotli4jConfiguration.setCanBeConsumed(false);
        getJvmPluginServices().configureAsRuntimeElements(brotli4jConfiguration);

        brotli4jConfiguration.getAttributes().attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, host.getOperatingSystemFamily());
        brotli4jConfiguration.getAttributes().attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, host.getArchitecture());

        brotli4jConfiguration.defaultDependencies(dependencies -> {
            dependencies.add(getDependencyFactory().create("com.aayushatharva.brotli4j", "brotli4j", extension.getVersion().get()));
            dependencies.add(getDependencyFactory().create("com.aayushatharva.brotli4j", "natives", extension.getVersion().get()));
        });

        project.getDependencies().getComponents().withModule("com.aayushatharva.brotli4j:natives", Brotli4jRule.class);

        project.getTasks().withType(Brotli4jTask.class, task -> {
            task.getBrotli4jClasspath().from(brotli4jConfiguration);
        });

    }
}
