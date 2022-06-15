package io.freefair.gradle.plugins.lombok;

import io.freefair.gradle.plugins.lombok.internal.ConfigUtil;
import io.freefair.gradle.plugins.lombok.tasks.LombokJarTask;
import io.freefair.gradle.plugins.lombok.tasks.LombokTask;
import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

@Getter
public class LombokBasePlugin implements Plugin<Project> {

    private LombokExtension lombokExtension;
    private Configuration lombokConfiguration;

    @Override
    public void apply(Project project) {
        lombokExtension = project.getExtensions().create("lombok", LombokExtension.class);
        lombokExtension.getDisableConfig().convention(ConfigUtil.isDisableConfig(project));

        lombokConfiguration = project.getConfigurations().create("lombok");
        lombokConfiguration.defaultDependencies(dependencySet -> dependencySet.add(
                project.getDependencies().create("org.projectlombok:lombok:" + lombokExtension.getVersion().get())
        ));

        project.getTasks().withType(LombokTask.class)
                .configureEach(lombokTask -> lombokTask.getLombokClasspath().from(lombokConfiguration));

        project.getTasks().withType(LombokJarTask.class)
                .configureEach(lombokJarTask -> {
                    lombokJarTask.getArchiveVersion().convention(lombokExtension.getVersion());
                    lombokJarTask.getDestinationDirectory().convention(project.getLayout().getBuildDirectory().dir("lombok"));
                });

    }
}
