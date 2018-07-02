package io.freefair.gradle.plugins.jsass;

import com.google.gson.Gson;
import io.bit3.jsass.*;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.annotation.DebugFunction;
import io.bit3.jsass.annotation.ErrorFunction;
import io.bit3.jsass.annotation.WarnFunction;
import io.bit3.jsass.importer.Importer;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.api.GradleException;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.file.*;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Lars Grefer
 */
@Getter
@Setter
public class SassCompile extends SourceTask {

    public SassCompile() {
        include("**/*.scss");
        include("**/*.sass");
    }

    @OutputFiles
    protected FileTree getOutputFiles() {
        ConfigurableFileTree files = getProject().fileTree(destinationDir);
        files.include("**/*.css");
        files.include("**/*.css.map");
        return files;
    }

    @Internal
    private final DirectoryProperty destinationDir = newOutputDirectory();

    @TaskAction
    public void compileSass() {
        Compiler compiler = new Compiler();
        Options options = new Options();

        options.setFunctionProviders(new ArrayList<>(getFunctionProviders()));
        options.getFunctionProviders().add(new LoggingFunctionProvider());
        options.setHeaderImporters(getHeaderImporters());
        options.setImporters(getImporters());
        if (getIncludePaths() != null) {
            options.setIncludePaths(new ArrayList<>(getIncludePaths().getFiles()));
        }
        options.setIndent(indent.get());
        options.setLinefeed(linefeed.get());
        options.setOmitSourceMapUrl(omitSourceMapUrl.get());
        options.setOutputStyle(outputStyle.get());
        options.setPluginPath(getPluginPath());
        options.setPrecision(precision.get());
        options.setSourceComments(sourceComments.get());
        options.setSourceMapContents(sourceMapContents.get());
        options.setSourceMapEmbed(sourceMapEmbed.get());
        options.setSourceMapRoot(getSourceMapRoot());

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
                    File fakeOut = new File(
                            fileVisitDetails.getFile().getParentFile(),
                            name.substring(0, name.length() - 5) + ".css"
                    );
                    File realMap = new File(getDestinationDir().get().getAsFile(), pathString + ".map");
                    File fakeMap = new File(fakeOut.getPath() + ".map");

                    options.setIsIndentedSyntaxSrc(name.endsWith(".sass"));

                    if (sourceMapEnabled.get()) {
                        options.setSourceMapFile(fakeMap.toURI());
                    } else {
                        options.setSourceMapFile(null);
                    }

                    try {
                        URI inputPath = in.getAbsoluteFile().toURI();

                        Output output = compiler.compileFile(inputPath, fakeOut.toURI(), options);

                        if (realOut.getParentFile().exists() || realOut.getParentFile().mkdirs()) {
                            ResourceGroovyMethods.write(realOut, output.getCss());
                        } else {
                            getLogger().error("Cannot write into {}", realOut.getParentFile());
                            throw new GradleException("Cannot write into " + realMap.getParentFile());
                        }
                        if (sourceMapEnabled.get()) {
                            if (realMap.getParentFile().exists() || realMap.getParentFile().mkdirs()) {
                                ResourceGroovyMethods.write(realMap, output.getSourceMap());
                            } else {
                                getLogger().error("Cannot write into {}", realMap.getParentFile());
                                throw new GradleException("Cannot write into " + realMap.getParentFile());
                            }
                        }
                    } catch (CompilationException e) {
                        SassError sassError = new Gson().fromJson(e.getErrorJson(), SassError.class);

                        getLogger().error("{}:{}:{}", sassError.getFile(), sassError.getLine(), sassError.getColumn());
                        getLogger().error(e.getErrorMessage());

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
    private List<Object> functionProviders = new LinkedList<>();

    @Input
    @Optional
    private List<Importer> headerImporters = new LinkedList<>();

    /**
     * Custom import functions.
     */
    @Input
    @Optional
    private Collection<Importer> importers = new LinkedList<>();

    /**
     * SassList of paths.
     */
    @InputFiles
    @Optional
    private final ConfigurableFileCollection includePaths = getProject().files();

    @Input
    private final Property<String> indent = getProject().getObjects().property(String.class);

    @Input
    private final Property<String> linefeed = getProject().getObjects().property(String.class);

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

    @Input
    @Optional
    private String pluginPath;

    /**
     * Precision for outputting fractional numbers.
     */
    @Input
    private final Property<Integer> precision = getProject().getObjects().property(Integer.class);

    /**
     * If you want inline source comments.
     */
    @Input
    private final Property<Boolean> sourceComments = getProject().getObjects().property(Boolean.class);

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
    private URI sourceMapRoot;

    public class LoggingFunctionProvider {

        @WarnFunction
        @SuppressWarnings("unused")
        public void warn(String message) {
            getLogger().warn(message);
        }

        @ErrorFunction
        @SuppressWarnings("unused")
        public void error(String message) {
            getLogger().error(message);
        }

        @DebugFunction
        @SuppressWarnings("unused")
        public void debug(String message) {
            getLogger().info(message);
        }
    }
}
