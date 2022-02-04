package io.freefair.gradle.plugins.lombok.internal;

import io.freefair.gradle.plugins.lombok.tasks.LombokConfig;
import lombok.experimental.UtilityClass;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

@UtilityClass
public class ConfigUtil {

    public Map<File, TaskProvider<LombokConfig>> getLombokConfigTasks(Project project, String sourceSetName, Set<File> srcDirs) {
        Map<File, TaskProvider<LombokConfig>> result = new HashMap<>();

        int i = 1;
        for (File srcDir : srcDirs) {

            int finalI = i;
            String taskName = "generate" + capitalize(sourceSetName) + "EffectiveLombokConfig" + i;
            TaskProvider<LombokConfig> genConfigTask = project.getTasks().register(taskName, LombokConfig.class, lombokConfigTask -> {
                lombokConfigTask.setGroup("lombok");
                lombokConfigTask.setDescription("Generate effective Lombok configuration for '" + srcDir + "' of source-set '" + sourceSetName + "'.");
                lombokConfigTask.getPaths().from(srcDir);
                lombokConfigTask.getOutputFile().set(project.getLayout().getBuildDirectory().file("lombok/effective-config/" + sourceSetName + "/lombok-" + finalI + ".config"));
            });

            result.put(srcDir, genConfigTask);

            i++;
        }

        return result;
    }
}
