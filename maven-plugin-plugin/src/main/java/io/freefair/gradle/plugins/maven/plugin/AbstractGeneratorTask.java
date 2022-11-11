package io.freefair.gradle.plugins.maven.plugin;

import org.apache.maven.plugin.descriptor.InvalidPluginDescriptorException;
import org.apache.maven.plugin.plugin.AbstractGeneratorMojo;
import org.apache.maven.tools.plugin.extractor.ExtractionException;
import org.apache.maven.tools.plugin.generator.GeneratorException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

/**
 * @author Lars Grefer
 * @see AbstractGeneratorMojo
 */
@SuppressWarnings("JavadocReference")
public abstract class AbstractGeneratorTask extends DefaultTask {

    /**
     * The goal prefix that will appear before the ":".
     *
     * @see AbstractGeneratorMojo#goalPrefix
     */
    @Optional
    @Input
    public abstract Property<String> getGoalPrefix();

    /**
     * @see AbstractGeneratorMojo#execute()
     */
    @TaskAction
    public void execute() throws GeneratorException, IOException, XmlPullParserException, InvalidPluginDescriptorException, ExtractionException {

        generate();
    }

    protected abstract void generate() throws ExtractionException, InvalidPluginDescriptorException, XmlPullParserException, IOException, GeneratorException;

}
