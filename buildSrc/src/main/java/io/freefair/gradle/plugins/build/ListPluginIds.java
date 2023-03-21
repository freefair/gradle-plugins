package io.freefair.gradle.plugins.build;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.plugin.devel.PluginDeclaration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;


public abstract class ListPluginIds extends DefaultTask {

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void listIds() throws IOException {
        List<String> ids = new ArrayList<>();

        for (Project subproject : getProject().getRootProject().getAllprojects()) {

            GradlePluginDevelopmentExtension extension = subproject.getExtensions().findByType(GradlePluginDevelopmentExtension.class);

            if (extension != null) {
                for (PluginDeclaration plugin : extension.getPlugins()) {
                    ids.add(plugin.getId());
                }
            }
        }

        Files.write(getOutputFile().get().getAsFile().toPath(), ids, StandardCharsets.UTF_8);
    }
}
