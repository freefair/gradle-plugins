package io.freefair.gradle.plugins.maven.plugin;

import io.freefair.gradle.plugins.maven.plugin.wrappers.MavenLogWrapper;
import lombok.Getter;
import org.apache.maven.tools.plugin.generator.Generator;
import org.apache.maven.tools.plugin.generator.PluginDescriptorGenerator;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;

import javax.inject.Inject;
import java.io.File;

/**
 * @author Lars Grefer
 * @see org.apache.maven.plugin.plugin.DescriptorGeneratorMojo
 */
@Getter
public abstract class DescriptorGeneratorTask extends AbstractGeneratorTask {

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @Override
    @Internal
    protected Generator getGenerator() {
        return new PluginDescriptorGenerator(new MavenLogWrapper(getLogger()));
    }

    @Override
    @Internal
    protected File getBaseDir() {
        return getOutputDirectory().dir("META-INF/maven").get().getAsFile();
    }
}
