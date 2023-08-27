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
import org.gradle.api.UncheckedIOException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.EmptyFileVisitor;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.webjars.WebJarAssetLocator;
import sass.embedded_protocol.EmbeddedSass;
import sass.embedded_protocol.EmbeddedSass.OutboundMessage.CompileResponse.CompileSuccess;
import sass.embedded_protocol.EmbeddedSass.OutboundMessage.VersionResponse;
import sass.embedded_protocol.EmbeddedSass.OutputStyle;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.LinkedHashSet;

@Getter
@Setter
@CacheableTask
public abstract class SassCompile extends SourceTask {

    public SassCompile() {
        include("**/*.scss");
        include("**/*.sass");

        ExtraPropertiesExtension extraProperties = new DslObject(this).getExtensions().getExtraProperties();
        for (OutputStyle value : OutputStyle.values()) {
            extraProperties.set(value.name(), value);
        }
    }

    @OutputDirectory
    public abstract DirectoryProperty getDestinationDir();

    @TaskAction
    public void compileSass() throws IOException {

        try (SassCompiler compiler = SassCompilerFactory.bundled()) {
            compiler.setOutputStyle(getOutputStyle().getOrNull());
            compiler.setGenerateSourceMaps(getSourceMapEnabled().getOrElse(true));
            compiler.setSourceMapIncludeSources(getSourceMapContents().getOrElse(false));

            compiler.setLoggingHandler(new Slf4jLoggingHandler(getLogger()));
            compiler.getLoadPaths().addAll(getIncludePaths().getFiles());

            getFileImporters().get().forEach(compiler::registerImporter);
            getCustomImporters().get().forEach(compiler::registerImporter);
            getHostFunctions().get().forEach(compiler::registerFunction);

            if (!getWebjars().isEmpty()) {
                LinkedHashSet<URL> urls = new LinkedHashSet<>();

                for (File webjar : getWebjars()) {
                    urls.add(webjar.toURI().toURL());
                }

                URLClassLoader webjarsLoader = new URLClassLoader(urls.toArray(new URL[0]));
                compiler.registerImporter(new WebjarsImporter(webjarsLoader, new WebJarAssetLocator(webjarsLoader)).autoCanonicalize());
            }

            VersionResponse version = compiler.getVersion();
            getLogger().info("{}", version);

            getSource().visit(new EmptyFileVisitor() {

                @Override
                public void visitFile(@Nonnull FileVisitDetails fileVisitDetails) {
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

                                if (getSourceMapEnabled().get()) {
                                    String mapUrl;

                                    if (getSourceMapEmbed().get()) {
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
                            if (getSourceMapEnabled().get() && !getSourceMapEmbed().get()) {
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
    }

    /**
     * Custom import functions.
     */
    @Input
    @Optional
    public abstract ListProperty<HostFunction> getHostFunctions();

    @Input
    @Optional
    public abstract ListProperty<FileImporter> getFileImporters();

    /**
     * Custom import functions.
     */
    @Input
    @Optional
    public abstract ListProperty<CustomImporter> getCustomImporters();

    @InputFiles
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getWebjars();

    /**
     * SassList of paths.
     */
    @InputFiles
    @Optional
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getIncludePaths();

    /**
     * Disable sourceMappingUrl in css output.
     */
    @Input
    public abstract Property<Boolean> getOmitSourceMapUrl();

    /**
     * Output style for the generated css code.
     */
    @Input
    private final Property<OutputStyle> outputStyle = getProject().getObjects().property(OutputStyle.class);

    /**
     * Embed include contents in maps.
     */
    @Input
    public abstract Property<Boolean> getSourceMapContents();

    /**
     * Embed sourceMappingUrl as data uri.
     */
    @Input
    public abstract Property<Boolean> getSourceMapEmbed();

    @Input
    public abstract Property<Boolean> getSourceMapEnabled();

    @Input
    @Optional
    public abstract Property<URI> getSourceMapRoot();

    public void setOutputStyle(String outputStyle) {
        getOutputStyle().set(OutputStyle.valueOf(outputStyle.trim().toUpperCase()));
    }
}
