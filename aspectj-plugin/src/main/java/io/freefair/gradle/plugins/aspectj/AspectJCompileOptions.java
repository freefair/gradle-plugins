package io.freefair.gradle.plugins.aspectj;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.CommandLineArgumentProvider;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Compilation options to be passed to the AspectJ compiler.
 *
 * @author Lars Grefer
 * @see org.gradle.api.tasks.compile.GroovyCompileOptions
 * @see org.gradle.api.tasks.scala.ScalaCompileOptions
 */
@Getter
@Setter
public class AspectJCompileOptions implements Serializable {

    /**
     * Accept as source bytecode any .class files in the .jar files or directories on Path.
     * The output will include these classes, possibly as woven with any applicable aspects.
     * Path is a single argument containing a list of paths to zip files or directories.
     */
    @Classpath
    @SkipWhenEmpty
    private final ConfigurableFileCollection inpath;

    /**
     * Weave binary aspects from jar files and directories on path into all sources.
     * The aspects should have been output by the same version of the compiler.
     * When running the output classes, the run classpath should contain all aspectpath entries.
     * Path, like classpath, is a single argument containing a list of paths to jar files.
     */
    @Classpath
    private final ConfigurableFileCollection aspectpath;

    /**
     * Put output classes in zip file output.jar.
     */
    @OutputFile
    @Optional
    private final RegularFileProperty outjar;

    /**
     * Generate aop xml file for load-time weaving with default name (META-INF/aop-ajc.xml).
     */
    @Input
    private final Property<Boolean> outxml;

    /**
     * Generate aop.xml file for load-time weaving with custom name.
     */
    @Input
    @Optional
    private final Property<String> outxmlfile;

    /**
     * Find and build all .java or .aj source files under any directory listed in DirPaths.
     * DirPaths, like classpath, is a single argument containing a list of paths to directories.
     */
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFiles
    private final ConfigurableFileCollection sourceroots;

    /**
     * Generate a build .ajsym file into the output directory.
     * Used for viewing crosscutting references by tools like the AspectJ Browser.
     */
    @Input
    private final Property<Boolean> crossrefs;

    /**
     * Override location of VM's bootclasspath for purposes of evaluating types when compiling.
     * Path is a single argument containing a list of paths to zip files or directories.
     */
    @Classpath
    private final ConfigurableFileCollection bootclasspath;

    /**
     * Override location of VM's extension directories for purposes of evaluating types when compiling.
     * Path is a single argument containing a list of paths to directories.
     */
    @Classpath
    private final ConfigurableFileCollection extdirs;

    /**
     * @see <a href="https://stackoverflow.com/a/71120602/3574494">https://stackoverflow.com/a/71120602/3574494</a>
     */
    @InputFile
    @PathSensitive(PathSensitivity.NAME_ONLY)
    @Optional
    private final RegularFileProperty xmlConfigured;

    /**
     * Specify default source encoding format.
     */
    @Input
    @Optional
    private final Property<String> encoding;

    /**
     * Emit messages about accessed/processed compilation units.
     */
    @Console
    private final Property<Boolean> verbose;

    /**
     * Any additional arguments to be passed to the compiler.
     */
    @Input
    private List<String> compilerArgs = new ArrayList<>();

    @Input
    private List<CommandLineArgumentProvider> compilerArgumentProviders = new ArrayList<>();

    /**
     * Options for running the compiler in a child process.
     */
    @Internal
    private final AjcForkOptions forkOptions;

    public void forkOptions(Action<AjcForkOptions> action) {
        action.execute(getForkOptions());
    }

    @Inject
    public AspectJCompileOptions(ObjectFactory objectFactory) {
        inpath = objectFactory.fileCollection();
        aspectpath = objectFactory.fileCollection();
        outjar = objectFactory.fileProperty();
        outxml = objectFactory.property(Boolean.class).convention(false);
        outxmlfile = objectFactory.property(String.class);
        sourceroots = objectFactory.fileCollection();
        crossrefs = objectFactory.property(Boolean.class).convention(false);
        bootclasspath = objectFactory.fileCollection();
        extdirs = objectFactory.fileCollection();
        xmlConfigured = objectFactory.fileProperty();
        encoding = objectFactory.property(String.class);
        verbose = objectFactory.property(Boolean.class).convention(false);
        forkOptions = objectFactory.newInstance(AjcForkOptions.class);
    }
}
