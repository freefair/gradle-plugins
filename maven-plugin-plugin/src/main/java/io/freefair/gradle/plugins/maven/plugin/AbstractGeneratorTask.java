package io.freefair.gradle.plugins.maven.plugin;

import io.freefair.gradle.plugins.maven.plugin.wrappers.MavenProjectWrapper;
import io.freefair.gradle.plugins.maven.plugin.wrappers.MojoAnnotationScannerWrapper;
import io.freefair.gradle.plugins.maven.plugin.wrappers.PlexusLoggerWrapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.artifact.factory.DefaultArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.DefaultArtifactResolver;
import org.apache.maven.plugin.descriptor.InvalidPluginDescriptorException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.plugin.AbstractGeneratorMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.tools.plugin.DefaultPluginToolsRequest;
import org.apache.maven.tools.plugin.extractor.ExtractionException;
import org.apache.maven.tools.plugin.extractor.MojoDescriptorExtractor;
import org.apache.maven.tools.plugin.extractor.annotations.JavaAnnotationsMojoDescriptorExtractor;
import org.apache.maven.tools.plugin.extractor.annotations.scanner.DefaultMojoAnnotationsScanner;
import org.apache.maven.tools.plugin.generator.Generator;
import org.apache.maven.tools.plugin.generator.GeneratorException;
import org.apache.maven.tools.plugin.scanner.DefaultMojoScanner;
import org.apache.maven.tools.plugin.scanner.MojoScanner;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.DefaultArchiverManager;
import org.codehaus.plexus.component.repository.ComponentDependency;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author Lars Grefer
 * @see AbstractGeneratorMojo
 */
@Getter
@Setter
public abstract class AbstractGeneratorTask extends DefaultTask {

    @InputFiles
    private final ConfigurableFileCollection sourceDirectories = getProject().files();

    @InputFiles
    private final ConfigurableFileCollection classesDirectories = getProject().files();

    @InputFile
    private final RegularFileProperty pomFile = getProject().getObjects().fileProperty();

    /**
     * The file encoding of the source files.
     *
     * @see AbstractGeneratorMojo#encoding
     */
    @Optional
    @Input
    private final Property<String> encoding = getProject().getObjects().property(String.class);

    /**
     * The goal prefix that will appear before the ":".
     *
     * @see AbstractGeneratorMojo#goalPrefix
     */
    @Optional
    @Input
    private final Property<String> goalPrefix = getProject().getObjects().property(String.class);

    /**
     * @see AbstractGeneratorMojo#skipErrorNoDescriptorsFound
     */
    @Input
    private final Property<Boolean> skipErrorNoDescriptorsFound = getProject().getObjects().property(Boolean.class).convention(false);

    /**
     * @see AbstractGeneratorMojo#execute()
     */
    @TaskAction
    public void generate() throws GeneratorException, IOException, XmlPullParserException, InvalidPluginDescriptorException, ExtractionException {
        PluginDescriptor pluginDescriptor = new PluginDescriptor();

        MavenProject project = new MavenProjectWrapper(getProject(), getPomFile().getAsFile().get());

        pluginDescriptor.setName(project.getName());
        pluginDescriptor.setDescription(project.getDescription());
        pluginDescriptor.setGroupId(project.getGroupId());
        pluginDescriptor.setVersion(project.getVersion());
        pluginDescriptor.setArtifactId(project.getArtifactId());
        pluginDescriptor.setGoalPrefix(goalPrefix.getOrElse(PluginDescriptor.getGoalPrefixFromArtifactId(project.getArtifactId())));

        List<ComponentDependency> deps = getRuntimeDependencies();
        pluginDescriptor.setDependencies(deps);

        DefaultPluginToolsRequest request = new DefaultPluginToolsRequest(project, pluginDescriptor);

        if (getEncoding().isPresent()) {
            request.setEncoding(getEncoding().getOrNull());
        }
        request.setSkipErrorNoDescriptorsFound(getSkipErrorNoDescriptorsFound().get());

        MojoScanner mojoScanner = getMojoScanner();
        mojoScanner.populatePluginDescriptor(request);

        getBaseDir().mkdirs();

        getGenerator().execute(
                getBaseDir(),
                request
        );
    }

    @Nonnull
    private List<ComponentDependency> getRuntimeDependencies() {
        return getProject().getConfigurations().getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME)
                .getResolvedConfiguration()
                .getResolvedArtifacts()
                .stream()
                .map(resolvedDependency -> {
                    ComponentDependency componentDependency = new ComponentDependency();

                    componentDependency.setArtifactId(resolvedDependency.getModuleVersion().getId().getName());
                    componentDependency.setGroupId(resolvedDependency.getModuleVersion().getId().getGroup());
                    componentDependency.setVersion(resolvedDependency.getModuleVersion().getId().getVersion());
                    componentDependency.setType(resolvedDependency.getType());

                    return componentDependency;
                })
                .collect(Collectors.toList());
    }

    @Internal
    protected abstract Generator getGenerator();

    @Internal
    protected abstract File getBaseDir();

    private MojoScanner getMojoScanner() {
        Map<String, MojoDescriptorExtractor> extractors = new TreeMap<>();
        JavaAnnotationsMojoDescriptorExtractor mojoDescriptorExtractor = new JavaAnnotationsMojoDescriptorExtractor();

        DefaultMojoAnnotationsScanner delegate = new DefaultMojoAnnotationsScanner();
        delegate.enableLogging(new PlexusLoggerWrapper(getLogger()));
        MojoAnnotationScannerWrapper mojoAnnotationsScanner = new MojoAnnotationScannerWrapper(delegate);
        mojoAnnotationsScanner.setSourceDirectories(sourceDirectories);
        mojoAnnotationsScanner.setClassesDirectories(classesDirectories);

        ArtifactResolver artifactResolver = new DefaultArtifactResolver();
        DefaultArtifactFactory artifactFactory = new DefaultArtifactFactory();
        ArchiverManager archiverManager = new DefaultArchiverManager();

        Map<String, Object> values = new HashMap<>();
        values.put("mojoAnnotationsScanner", mojoAnnotationsScanner);
        values.put("artifactResolver", artifactResolver);
        values.put("artifactFactory", artifactFactory);
        values.put("archiverManager", archiverManager);

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

        extractors.put("java-annotations", mojoDescriptorExtractor);
        DefaultMojoScanner defaultMojoScanner = new DefaultMojoScanner(extractors);
        defaultMojoScanner.enableLogging(new PlexusLoggerWrapper(getLogger()));
        return defaultMojoScanner;
    }
}
