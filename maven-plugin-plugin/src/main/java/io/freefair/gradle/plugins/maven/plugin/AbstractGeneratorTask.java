package io.freefair.gradle.plugins.maven.plugin;

import io.freefair.gradle.plugins.maven.plugin.wrappers.MavenProjectWrapper;
import io.freefair.gradle.plugins.maven.plugin.wrappers.MojoAnnotationScannerWrapper;
import io.freefair.gradle.plugins.maven.plugin.wrappers.PlexusLoggerWrapper;
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
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import javax.annotation.Nonnull;
import javax.inject.Inject;
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
@SuppressWarnings("JavadocReference")
public abstract class AbstractGeneratorTask extends DefaultTask {

    @Inject
    protected abstract ProjectLayout getProjectLayout();

    @InputFiles
    public abstract ConfigurableFileCollection getSourceDirectories();

    @InputFiles
    public abstract ConfigurableFileCollection getClassesDirectories();

    @InputFile
    public abstract RegularFileProperty getPomFile();

    /**
     * The file encoding of the source files.
     *
     * @see AbstractGeneratorMojo#encoding
     */
    @Optional
    @Input
    public abstract Property<String> getEncoding();

    /**
     * The goal prefix that will appear before the ":".
     *
     * @see AbstractGeneratorMojo#goalPrefix
     */
    @Optional
    @Input
    public abstract Property<String> getGoalPrefix();

    /**
     * @see AbstractGeneratorMojo#skipErrorNoDescriptorsFound
     */
    @Input
    public abstract Property<Boolean> getSkipErrorNoDescriptorsFound();

    public AbstractGeneratorTask() {
        getSkipErrorNoDescriptorsFound().convention(false);
    }

    /**
     * @see AbstractGeneratorMojo#execute()
     */
    @TaskAction
    public void generate() throws GeneratorException, IOException, XmlPullParserException, InvalidPluginDescriptorException, ExtractionException {
        PluginDescriptor pluginDescriptor = new PluginDescriptor();

        SourceSetContainer sourceSets = getProject().getExtensions().getByType(JavaPluginExtension.class).getSourceSets();
        MavenProject project = new MavenProjectWrapper(getProjectLayout(), sourceSets, getPomFile().getAsFile().get());

        pluginDescriptor.setName(project.getName());
        pluginDescriptor.setDescription(project.getDescription());
        pluginDescriptor.setGroupId(project.getGroupId());
        pluginDescriptor.setVersion(project.getVersion());
        pluginDescriptor.setArtifactId(project.getArtifactId());
        pluginDescriptor.setGoalPrefix(getGoalPrefix().getOrElse(PluginDescriptor.getGoalPrefixFromArtifactId(project.getArtifactId())));

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

    /**
     * @see AbstractGeneratorMojo#mojoScanner
     */
    private MojoScanner getMojoScanner() {
        Map<String, MojoDescriptorExtractor> extractors = new TreeMap<>();

        extractors.put("java-annotations", getMojoDescriptorExtractor());

        DefaultMojoScanner defaultMojoScanner = new DefaultMojoScanner(extractors);
        defaultMojoScanner.enableLogging(new PlexusLoggerWrapper(getLogger()));
        return defaultMojoScanner;
    }

    private JavaAnnotationsMojoDescriptorExtractor getMojoDescriptorExtractor() {
        PlexusLoggerWrapper plexusLoggerWrapper = new PlexusLoggerWrapper(getLogger());

        JavaAnnotationsMojoDescriptorExtractor mojoDescriptorExtractor = new JavaAnnotationsMojoDescriptorExtractor();
        mojoDescriptorExtractor.enableLogging(plexusLoggerWrapper);

        DefaultMojoAnnotationsScanner delegate = new DefaultMojoAnnotationsScanner();
        delegate.enableLogging(plexusLoggerWrapper);

        MojoAnnotationScannerWrapper mojoAnnotationsScanner = new MojoAnnotationScannerWrapper(delegate);
        mojoAnnotationsScanner.setSourceDirectories(getSourceDirectories());
        mojoAnnotationsScanner.setClassesDirectories(getClassesDirectories());

        ArchiverManager archiverManager = new DefaultArchiverManager();

        Map<String, Object> values = new HashMap<>();
        values.put("mojoAnnotationsScanner", mojoAnnotationsScanner);
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

        return mojoDescriptorExtractor;
    }
}
