package io.freefair.gradle.plugins.sass;

import de.larsgrefer.sass.embedded.SassCompilationFailedException;
import de.larsgrefer.sass.embedded.SassCompiler;
import de.larsgrefer.sass.embedded.SassCompilerFactory;
import de.larsgrefer.sass.embedded.functions.HostFunction;
import de.larsgrefer.sass.embedded.importer.CustomImporter;
import de.larsgrefer.sass.embedded.importer.FileImporter;
import de.larsgrefer.sass.embedded.importer.WebjarsImporter;
import de.larsgrefer.sass.embedded.logging.Slf4jLoggingHandler;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.GradleException;
import org.gradle.api.Incubating;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.file.*;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.Optional;
import org.webjars.WebJarAssetLocator;
import sass.embedded_protocol.EmbeddedSass;
import sass.embedded_protocol.EmbeddedSass.InboundMessage.CompileRequest.OutputStyle;
import sass.embedded_protocol.EmbeddedSass.OutboundMessage.CompileResponse.CompileSuccess;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

@Getter
@Setter
@CacheableTask
@Incubating
public class SassCompile extends SourceTask {

    public SassCompile() {
        include("**/*.scss");
        include("**/*.sass");

        ExtraPropertiesExtension extraProperties = new DslObject(this).getExtensions().getExtraProperties();
        for (OutputStyle value : OutputStyle.values()) {
            extraProperties.set(value.name(), value);
        }
    }

    @OutputFiles
    protected FileTree getOutputFiles() {
        ConfigurableFileTree files = getProject().fileTree(destinationDir);
        files.include("**/*.css");
        files.include("**/*.css.map");
        return files;
    }

    @Internal
    private final DirectoryProperty destinationDir = getProject().getObjects().directoryProperty();

    @TaskAction
    public void compileSass() throws IOException {

        SassCompiler compiler = SassCompilerFactory.bundled();
        compiler.setOutputStyle(getOutputStyle().getOrNull());
        compiler.setGenerateSourceMaps(sourceMapEnabled.getOrElse(true));

        compiler.setLoggingHandler(new Slf4jLoggingHandler(getLogger()));
        compiler.getLoadPaths().addAll(getIncludePaths().getFiles());

        fileImporters.get().forEach(compiler::registerImporter);
        customImporters.get().forEach(compiler::registerImporter);
        hostFunctions.get().forEach(compiler::registerFunction);

        if(!webjars.isEmpty()) {
            LinkedHashSet<URL> urls = new LinkedHashSet<>();

            for (File webjar : webjars) {
                urls.add(webjar.toURI().toURL());
            }

            URLClassLoader webjarsLoader = new URLClassLoader(urls.toArray(new URL[0]));
            compiler.registerImporter(new WebjarsImporter(webjarsLoader, new WebJarAssetLocator(webjarsLoader)).autoCanonicalize());
        }

        getSource().visit(new FileVisitor() {
            @Override
            public void visitDir(FileVisitDetails fileVisitDetails) {

            }

            @Override
            public void visitFile(FileVisitDetails fileVisitDetails) {
                String name = fileVisitDetails.getName();
                if (name.startsWith("_"))
                    return;

                if (name.endsWith(".scss") || name.endsWith(".sass")) {
                    File in = fileVisitDetails.getFile();

                    String pathString = fileVisitDetails.getRelativePath().getPathString();

                    pathString = pathString.substring(0, pathString.length() - 5) + ".css";

                    File realOut = new File(getDestinationDir().get().getAsFile(), pathString);
                    File realMap = new File(getDestinationDir().get().getAsFile(), pathString + ".map");

                    try {

                        CompileSuccess output = compiler.compileFile(in, getOutputStyle().getOrNull());

                        if (realOut.getParentFile().exists() || realOut.getParentFile().mkdirs()) {
                            String css = output.getCss();

                            if (sourceMapEnabled.get()) {
                                String mapUrl;

                                if (sourceMapEmbed.get()) {
                                    mapUrl = "data:application/json;base64," + Base64.getEncoder().encodeToString(output.getSourceMapBytes().toByteArray());
                                }
                                else {
                                    mapUrl = realMap.getName();
                                }

                                css += "\n/*# sourceMappingURL=" + mapUrl + " */";
                            }

                            Files.write(realOut.toPath(), css.getBytes(StandardCharsets.UTF_8));
                        }
                        else {
                            getLogger().error("Cannot write into {}", realOut.getParentFile());
                            throw new GradleException("Cannot write into " + realMap.getParentFile());
                        }
                        if (sourceMapEnabled.get() && !sourceMapEmbed.get()) {
                            if (realMap.getParentFile().exists() || realMap.getParentFile().mkdirs()) {
                                Files.write(realMap.toPath(), output.getSourceMap().getBytes(StandardCharsets.UTF_8));
                            }
                            else {
                                getLogger().error("Cannot write into {}", realMap.getParentFile());
                                throw new GradleException("Cannot write into " + realMap.getParentFile());
                            }
                        }
                    } catch (SassCompilationFailedException e) {
                        EmbeddedSass.OutboundMessage.CompileResponse.CompileFailure sassError = e.getCompileFailure();

                        getLogger().error(sassError.getMessage());

                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        getLogger().error(e.getLocalizedMessage());
                        throw new UncheckedIOException(e);
                    }
                }
            }
        });
    }

    /**
     * Custom import functions.
     */
    @Input
    @Optional
    private ListProperty<HostFunction> hostFunctions = getProject().getObjects().listProperty(HostFunction.class);

    @Input
    @Optional
    private ListProperty<FileImporter> fileImporters = getProject().getObjects().listProperty(FileImporter.class);

    /**
     * Custom import functions.
     */
    @Input
    @Optional
    private ListProperty<CustomImporter> customImporters = getProject().getObjects().listProperty(CustomImporter.class);

    @InputFiles
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    private final ConfigurableFileCollection webjars = getProject().files();

    /**
     * SassList of paths.
     */
    @InputFiles
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    private final ConfigurableFileCollection includePaths = getProject().files();

    /**
     * Disable sourceMappingUrl in css output.
     */
    @Input
    private final Property<Boolean> omitSourceMapUrl = getProject().getObjects().property(Boolean.class);

    /**
     * Output style for the generated css code.
     */
    @Input
    private final Property<OutputStyle> outputStyle = getProject().getObjects().property(OutputStyle.class);

    /**
     * Embed include contents in maps.
     */
    @Input
    private final Property<Boolean> sourceMapContents = getProject().getObjects().property(Boolean.class);

    /**
     * Embed sourceMappingUrl as data uri.
     */
    @Input
    private final Property<Boolean> sourceMapEmbed = getProject().getObjects().property(Boolean.class);

    @Input
    private final Property<Boolean> sourceMapEnabled = getProject().getObjects().property(Boolean.class);

    @Input
    @Optional
    private final Property<URI> sourceMapRoot = getProject().getObjects().property(URI.class);

    public void setOutputStyle(String outputStyle) {
        this.outputStyle.set(OutputStyle.valueOf(outputStyle.trim().toUpperCase()));
    }
}
