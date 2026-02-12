package io.freefair.gradle.plugins.maven.plugin;

import io.freefair.gradle.plugins.maven.plugin.internal.MavenHelper;
import io.freefair.gradle.plugins.maven.plugin.wrappers.MavenProjectWrapper;
import io.freefair.gradle.plugins.maven.plugin.wrappers.MojoAnnotationScannerWrapper;
import io.freefair.gradle.plugins.maven.plugin.wrappers.PlexusLoggerWrapper;
import lombok.Getter;
import org.apache.maven.plugin.descriptor.InvalidPluginDescriptorException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.plugin.DescriptorGeneratorMojo;
import org.apache.maven.tools.plugin.DefaultPluginToolsRequest;
import org.apache.maven.tools.plugin.extractor.ExtractionException;
import org.apache.maven.tools.plugin.extractor.MojoDescriptorExtractor;
import org.apache.maven.tools.plugin.extractor.annotations.JavaAnnotationsMojoDescriptorExtractor;
import org.apache.maven.tools.plugin.extractor.annotations.converter.JavadocBlockTagsToXhtmlConverter;
import org.apache.maven.tools.plugin.extractor.annotations.converter.JavadocInlineTagsToXhtmlConverter;
import org.apache.maven.tools.plugin.extractor.annotations.scanner.DefaultMojoAnnotationsScanner;
import org.apache.maven.tools.plugin.extractor.annotations.scanner.MojoAnnotationsScanner;
import org.apache.maven.tools.plugin.generator.GeneratorException;
import org.apache.maven.tools.plugin.generator.PluginDescriptorFilesGenerator;
import org.apache.maven.tools.plugin.scanner.DefaultMojoScanner;
import org.apache.maven.tools.plugin.scanner.MojoScanner;
import org.codehaus.plexus.component.repository.ComponentDependency;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author Lars Grefer
 * @see DescriptorGeneratorMojo
 */
@Getter
public abstract class DescriptorGeneratorTask extends AbstractGeneratorTask {

    @Inject
    protected abstract ProjectLayout getProjectLayout();

    @InputFiles
    @SkipWhenEmpty
    public abstract ConfigurableFileCollection getSourceDirectories();

    @InputFiles
    @SkipWhenEmpty
    public abstract ConfigurableFileCollection getClassesDirectories();

    @InputFile
    public abstract RegularFileProperty getPomFile();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    /**
     * The file encoding of the source files.
     *
     * @see DescriptorGeneratorMojo#encoding
     */
    @Optional
    @Input
    public abstract Property<String> getEncoding();

    /**
     * @see DescriptorGeneratorMojo#skipErrorNoDescriptorsFound
     */
    @Input
    public abstract Property<Boolean> getSkipErrorNoDescriptorsFound();

    public DescriptorGeneratorTask() {
        getSkipErrorNoDescriptorsFound().convention(false);
    }

    /**
     * @see DescriptorGeneratorMojo#generate()
     */
    @Override
    protected void generate() throws ExtractionException, InvalidPluginDescriptorException, XmlPullParserException, IOException, GeneratorException {
        PluginDescriptor pluginDescriptor = new PluginDescriptor();

        MavenProjectWrapper project = new MavenProjectWrapper(getProjectLayout(), getPomFile().getAsFile().get());
        project.setMainSourceDirs(getSourceDirectories());
        project.getBuild().setOutputDirectory(getClassesDirectories().getFiles().iterator().next().getAbsolutePath());

        pluginDescriptor.setGroupId(project.getGroupId());
        pluginDescriptor.setArtifactId(project.getArtifactId());
        pluginDescriptor.setVersion(project.getVersion());
        pluginDescriptor.setGoalPrefix(getGoalPrefix().getOrElse(PluginDescriptor.getGoalPrefixFromArtifactId(project.getArtifactId())));
        pluginDescriptor.setName(project.getName());
        pluginDescriptor.setDescription(project.getDescription());


        List<ComponentDependency> deps = getRuntimeDependencies();
        pluginDescriptor.setDependencies(deps);

        DefaultPluginToolsRequest request = new DefaultPluginToolsRequest(project, pluginDescriptor);

        if (getEncoding().isPresent()) {
            request.setEncoding(getEncoding().getOrNull());
        }
        request.setSkipErrorNoDescriptorsFound(getSkipErrorNoDescriptorsFound().get());

        MojoScanner mojoScanner = getMojoScanner();
        mojoScanner.populatePluginDescriptor(request);

        PluginDescriptorFilesGenerator pluginDescriptorGenerator = new PluginDescriptorFilesGenerator();
        pluginDescriptorGenerator.execute(getOutputDirectory().dir("META-INF/maven").get().getAsFile(), request);

    }

    @Nonnull
    private List<ComponentDependency> getRuntimeDependencies() throws XmlPullParserException, IOException {
        return MavenHelper.parsePom(getPomFile().get().getAsFile())
                .getDependencies()
                .stream()
                .map(resolvedDependency -> {
                    ComponentDependency componentDependency = new ComponentDependency();

                    componentDependency.setArtifactId(resolvedDependency.getArtifactId());
                    componentDependency.setGroupId(resolvedDependency.getGroupId());
                    componentDependency.setVersion(resolvedDependency.getVersion());
                    componentDependency.setType(resolvedDependency.getType());

                    return componentDependency;
                })
                .collect(Collectors.toList());
    }

    /**
     * @see DescriptorGeneratorMojo#mojoScanner
     */
    private MojoScanner getMojoScanner() {
        Map<String, MojoDescriptorExtractor> extractors = new TreeMap<>();

        extractors.put("java-annotations", getMojoDescriptorExtractor());

        DefaultMojoScanner defaultMojoScanner = new DefaultMojoScanner(extractors);
        //Logger plexusLogger = new PlexusLoggerWrapper(getLogger());
        //defaultMojoScanner.enableLogging(plexusLogger);
        return defaultMojoScanner;
    }

    private JavaAnnotationsMojoDescriptorExtractor getMojoDescriptorExtractor() {
        PlexusLoggerWrapper plexusLoggerWrapper = new PlexusLoggerWrapper(getLogger());

        JavaAnnotationsMojoDescriptorExtractor mojoDescriptorExtractor = new JavaAnnotationsMojoDescriptorExtractor();
        //mojoDescriptorExtractor.enableLogging(plexusLoggerWrapper);

        DefaultMojoAnnotationsScanner delegate = new DefaultMojoAnnotationsScanner();
        //delegate.enableLogging(plexusLoggerWrapper);

        MojoAnnotationScannerWrapper mojoAnnotationsScanner = new MojoAnnotationScannerWrapper(delegate);
        mojoAnnotationsScanner.setSourceDirectories(getSourceDirectories());
        mojoAnnotationsScanner.setClassesDirectories(getClassesDirectories());

        JavadocInlineTagsToXhtmlConverter javadocInlineTagsToXhtmlConverter = new JavadocInlineTagsToXhtmlConverter(MavenHelper.getJavadocInlineTagToHtmlConverters());
        JavadocBlockTagsToXhtmlConverter javadocBlockTagsToXhtmlConverter = new JavadocBlockTagsToXhtmlConverter(javadocInlineTagsToXhtmlConverter, MavenHelper.getJavadocBlockTagToHtmlConverters());

        Map<String, Object> values = new HashMap<>();
        values.put("mojoAnnotationsScanner", mojoAnnotationsScanner);
        values.put("archiverManager", MavenHelper.getArchiverManager());
        values.put("javadocInlineTagsToHtmlConverter", javadocInlineTagsToXhtmlConverter);
        values.put("javadocBlockTagsToHtmlConverter", javadocBlockTagsToXhtmlConverter);

        try {
            Class<JavaAnnotationsMojoDescriptorExtractor> clazz = JavaAnnotationsMojoDescriptorExtractor.class;
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                Field declaredField = clazz.getDeclaredField(entry.getKey());
                declaredField.setAccessible(true);
                declaredField.set(mojoDescriptorExtractor, entry.getValue());
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        return mojoDescriptorExtractor;
    }
}
