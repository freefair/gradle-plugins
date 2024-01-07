package io.freefair.gradle.plugins.lombok.internal;

import io.freefair.gradle.plugins.lombok.tasks.LombokConfig;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.deprecation.DeprecationLogger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

@Slf4j
@UtilityClass
public class ConfigUtil {

    private static final Pattern stopBubblingPattern = Pattern.compile("\\s*config\\.stopBubbling\\s*=\\s*true\\s*", Pattern.CASE_INSENSITIVE);
    private static final Pattern importPattern = Pattern.compile("\\s*import\\s*(.*?)\\s*", Pattern.CASE_INSENSITIVE);

    @Deprecated
    public Map<File, TaskProvider<LombokConfig>> getLombokConfigTasks(Project project, String sourceSetName, Set<File> srcDirs) {

        DeprecationLogger.deprecateMethod(ConfigUtil.class, "getLombokConfigTasks()")
                .willBeRemovedInGradle9()
                .undocumented()
                .nagUser();

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

    public TaskProvider<LombokConfig> getLombokConfigTask(Project project, SourceSet sourceSet) {

        String taskName = sourceSet.getTaskName("generate", "effectiveLombokConfig");

        return project.getTasks().register(taskName, LombokConfig.class, lombokConfigTask -> {
            lombokConfigTask.setGroup("lombok");
            lombokConfigTask.setDescription("Generate effective Lombok configuration for source-set '" + sourceSet.getName() + "'.");
            lombokConfigTask.getPaths().from(sourceSet.getJava().getSourceDirectories());
            lombokConfigTask.getOutputFile().set(project.getLayout().getBuildDirectory().file("lombok/effective-config/lombok-" + sourceSet.getName() + ".config"));
            lombokConfigTask.doLast("cleanLombokConfig", new CleanLombokConfig());
        });
    }

    public static Provider<Boolean> isDisableConfig(Project project) {
        return project.getProviders().gradleProperty("lombok.disableConfig")
                .orElse(project.getProviders().systemProperty("lombok.disableConfig"))
                .map(propValue -> !"false".equalsIgnoreCase(propValue.trim()))
                .orElse(false);
    }

    /**
     * Resolves the Set of configuration files which lombok would use for the given path.
     * <p>
     * May return null, if imports are used within the files which could not be resolved.
     */
    @Nullable
    public Set<File> resolveConfigFilesForPath(File path) throws IOException {
        //Walk up to first existing dir
        path = path.getAbsoluteFile();
        while ((path != null) && !path.isDirectory()) {
            path = path.getParentFile();
        }

        Set<File> result = new HashSet<>();

        File dir = path;

        while (dir != null) {
            File lombokConfigFile = new File(dir, "lombok.config");

            if (lombokConfigFile.isFile()) {
                result.add(lombokConfigFile);

                List<String> lines = Files.readAllLines(lombokConfigFile.toPath());

                for (String s : lines) {
                    Matcher matcher = importPattern.matcher(s);
                    if (matcher.matches()) {
                        log.info("{} imports {}", lombokConfigFile, matcher.group(1));
                        return null;
                    }
                }
                if (lines.stream().anyMatch(line -> stopBubblingPattern.matcher(line).matches())) {
                    return result;
                }
            }

            dir = dir.getParentFile();
        }

        return result;
    }

}
