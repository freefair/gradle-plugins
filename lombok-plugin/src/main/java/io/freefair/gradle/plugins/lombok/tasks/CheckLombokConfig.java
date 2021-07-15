package io.freefair.gradle.plugins.lombok.tasks;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.util.Collections;
import java.util.Map;

@Getter
@Setter
public class CheckLombokConfig extends DefaultTask {

    @Input
    private final String sourceSet;

    @Input
    @Optional
    private final MapProperty<File, String> configs = getProject().getObjects().mapProperty(File.class, String.class);

    @Input
    @Optional
    private final ListProperty<String> expectedConfigs = getProject().getObjects().listProperty(String.class);

    @Inject
    public CheckLombokConfig(SourceSet sourceSet) {
        this.sourceSet = sourceSet.getName();
        setGroup("verification");
        setDescription("Check lombok configuration for source set " + this.getSourceSet());
    }

    @TaskAction
    public void check() {
        for (Map.Entry<File, String> entry : configs.getOrElse(Collections.emptyMap()).entrySet()) {
            File dir = entry.getKey();
            String config = entry.getValue();

            for (String expected : expectedConfigs.getOrElse(Collections.emptyList())) {
                if (dir.exists() && (config == null || !config.contains(expected))) {
                    getLogger().warn("'{}' is not configured for '{}' of the {} source-set", expected, dir, sourceSet);
                }
            }
        }
    }
}
