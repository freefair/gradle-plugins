package io.freefair.gradle.plugins;

import lombok.Data;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.JavaExecSpec;

import java.util.Collections;
import java.util.stream.Collectors;

@Data
public class AjcOptions {

    public AjcOptions(Project project) {

        aspectjClasspath = project.getLayout().configurableFiles();
        inpath = project.getLayout().configurableFiles();
        aspectpath = project.getLayout().configurableFiles();
        outjar = project.getLayout().fileProperty();
        outxml = project.getObjects().property(Boolean.class);
        outxmlfile = project.getLayout().fileProperty();
        crossrefs = project.getObjects().property(Boolean.class);
        version = project.getObjects().property(Boolean.class);
        classpath = project.getLayout().configurableFiles();
        destinationDir = project.getLayout().directoryProperty();
        target = project.getObjects().property(String.class);
        source = project.getObjects().property(String.class);
        nowarn = project.getObjects().property(Boolean.class);
        warn = project.getObjects().listProperty(String.class);
        deprecation = project.getObjects().property(Boolean.class);
        noImportError = project.getObjects().property(Boolean.class);
        proceedOnError = project.getObjects().property(Boolean.class);
        g = project.getObjects().listProperty(String.class);
        preserveAllLocals = project.getObjects().property(Boolean.class);
        referenceInfo = project.getObjects().property(Boolean.class);
        encoding = project.getObjects().property(String.class);
        verbose = project.getObjects().property(Boolean.class);
        showWeaveInfo = project.getObjects().property(Boolean.class);
        log = project.getLayout().fileProperty();
        progress = project.getObjects().property(Boolean.class);
        time = project.getObjects().property(Boolean.class);
        XterminateAfterCompilation = project.getObjects().property(Boolean.class);
        XaddSerialVersionUID = project.getObjects().property(Boolean.class);
        XnoInline = project.getObjects().property(Boolean.class);
        XserializableAspects = project.getObjects().property(Boolean.class);
        XnotReweavable = project.getObjects().property(Boolean.class);
        files = project.getLayout().configurableFiles();
    }

    @Classpath
    @InputFiles
    private final ConfigurableFileCollection aspectjClasspath;

    /**
     * Accept as source bytecode any .class files in the .jar files or directories on Path.
     * The output will include these classes, possibly as woven with any applicable aspects. Path is a single argument containing a list of paths to zip files or directories, delimited by the platform-specific path delimiter.
     */
    @Classpath
    @InputFiles
    private final ConfigurableFileCollection inpath;

    /**
     * Weave binary aspects from jar files and directories on path into all sources.
     * The aspects should have been output by the same version of the compiler.
     * When running the output classes, the run classpath should contain all aspectpath entries.
     * Path, like classpath, is a single argument containing a list of paths to jar files, delimited by the platform- specific classpath delimiter.
     */
    @Classpath
    @InputFiles
    private final ConfigurableFileCollection aspectpath;

    /**
     * Put output classes in zip file output.jar.
     */
    @Optional
    @OutputFile
    private final RegularFileProperty outjar;

    /**
     * Generate aop xml file for load-time weaving with default name (META-INF/aop-ajc.xml).
     */
    @Input
    private final Property<Boolean> outxml;

    /**
     * Generate aop.xml file for load-time weaving with custom name.
     */
    @Optional
    @OutputFile
    private final RegularFileProperty outxmlfile;

    /**
     * Generate a build .ajsym file into the output directory.
     * Used for viewing crosscutting references by tools like the AspectJ Browser.
     */
    @Input
    private final Property<Boolean> crossrefs;

    /**
     * Emit the version of the AspectJ compiler.
     */
    @Input
    private final Property<Boolean> version;

    /**
     * Specify where to find user class files.
     * Path is a single argument containing a list of paths to zip files or directories, delimited by the platform-specific path delimiter.
     */
    @Classpath
    @InputFiles
    private final ConfigurableFileCollection classpath;

    /**
     * Specify where to place generated .class files.
     */
    @OutputDirectory
    private final DirectoryProperty destinationDir;

    @Input
    private final Property<String> target;

    @Input
    private final Property<String> source;

    /**
     * Emit no warnings (equivalent to '-warn:none') This does not suppress messages generated by declare warning or Xlint.
     */
    @Optional
    @Input
    private final Property<Boolean> nowarn;

    /**
     * Emit warnings for any instances of the comma-delimited list of questionable code (eg '-warn:unusedLocals,deprecation'):
     *
     *     constructorName        method with constructor name
     *     packageDefaultMethod   attempt to override package-default method
     *     deprecation            usage of deprecated type or member
     *     maskedCatchBlocks      hidden catch block
     *     unusedLocals           local variable never read
     *     unusedArguments        method argument never read
     *     unusedImports          import statement not used by code in file
     *     none                   suppress all compiler warnings
     *
     *
     * -warn:none does not suppress messages generated by declare warning or Xlint.
     */
    @Input
    private final ListProperty<String> warn;

    /**
     * Same as -warn:deprecation
     */
    @Input
    private final Property<Boolean> deprecation;

    /**
     * Emit no errors for unresolved imports
     */
    @Input
    private final Property<Boolean> noImportError;

    /**
     * Keep compiling after error, dumping class files with problem methods
     */
    @Input
    private final Property<Boolean> proceedOnError;

    /**
     * debug attributes level, that may take three forms:
     *
     *     -g         all debug info ('-g:lines,vars,source')
     *     -g:none    no debug info
     *     -g:{items} debug info for any/all of [lines, vars, source], e.g.,
     *                -g:lines,source
     *
     */
    @Input
    private final ListProperty<String> g;

    /**
     * Preserve all local variables during code generation (to facilitate debugging).
     */
    @Input
    private final Property<Boolean> preserveAllLocals;

    /**
     * Compute reference information.
     */
    @Input
    private final Property<Boolean> referenceInfo;

    /**
     * Specify default source encoding format.
     */
    @Optional
    @Input
    private final Property<String> encoding;


    @Input
    private final Property<Boolean> verbose;

    /**
     * Emit messages about weaving
     */
    @Input
    private final Property<Boolean> showWeaveInfo;

    /**
     * Specify a log file for compiler messages.
     */
    @OutputFile
    @Optional
    private final RegularFileProperty log;

    /**
     * Show progress (requires -log mode).
     */
    private final Property<Boolean> progress;

    /**
     * Display speed information.
     */
    @Input
    private final Property<Boolean> time;

    /**
     * Causes compiler to terminate before weaving
     */
    @Input
    private final Property<Boolean> XterminateAfterCompilation;

    /**
     * Causes the compiler to calculate and add the SerialVersionUID field to any type implementing Serializable that is affected by an aspect.
     * The field is calculated based on the class before weaving has taken place.
     */
    @Input
    private final Property<Boolean> XaddSerialVersionUID;

    /**
     * (Experimental) do not inline around advice
     */
    @Input
    private final Property<Boolean> XnoInline;

    /**
     * (Experimental) Normally it is an error to declare aspects Serializable. This option removes that restriction.
     */
    @Input
    private final Property<Boolean> XserializableAspects;

    /**
     * (Experimental) Create class files that can't be subsequently rewoven by AspectJ.
     */
    @Input
    private final Property<Boolean> XnotReweavable;

    @Optional
    @InputFiles
    private final ConfigurableFileCollection files;

    public void applyTo(JavaExecSpec ajc) {
        ajc.setClasspath(aspectjClasspath);
        ajc.setMain("org.aspectj.tools.ajc.Main");

        if (!inpath.isEmpty()) {
            ajc.args("-inpath", inpath.getAsPath());
        }

        if (!aspectpath.isEmpty()) {
            ajc.args("-aspectpath", aspectpath.getAsPath());
        }

        if (outjar.isPresent()) {
            ajc.args("-outjar", outjar.get().getAsFile().getAbsolutePath());
        }

        if (outxml.getOrElse(false)) {
            ajc.args("-outxml");
        }

        if (outxmlfile.isPresent()) {
            ajc.args("-outxmlfile", outxmlfile.get().getAsFile().getAbsolutePath());
        }

        if (crossrefs.getOrElse(false)) {
            ajc.args("-crossrefs");
        }

        if (version.getOrElse(false)) {
            ajc.args("-version");
        }

        if (!classpath.isEmpty()) {
            ajc.args("-classpath", classpath.getAsPath());
        }

        if (destinationDir.isPresent()) {
            ajc.args("-d", destinationDir.get().getAsFile().getAbsolutePath());
        }

        if (target.isPresent()) {
            ajc.args("-target", target.get());
        }

        if (source.isPresent()) {
            ajc.args("-source", source.get());
        }

        if (nowarn.getOrElse(false)) {
            ajc.args("-nowarn");
        }

        if (!warn.getOrElse(Collections.emptyList()).isEmpty()) {
            ajc.args("-warn:" + warn.get().stream().collect(Collectors.joining(",")));
        }

        if (deprecation.getOrElse(false)) {
            ajc.args("-deprecation");
        }

        if (noImportError.getOrElse(false)) {
            ajc.args("-noImportError");
        }

        if (proceedOnError.getOrElse(false)) {
            ajc.args("-proceedOnError");
        }

        if (!g.getOrElse(Collections.emptyList()).isEmpty()) {
            ajc.args("-g:" + g.get().stream().collect(Collectors.joining(",")));
        }

        if (preserveAllLocals.getOrElse(false)) {
            ajc.args("-preserveAllLocals");
        }

        if (referenceInfo.getOrElse(false)) {
            ajc.args("-referenceInfo");
        }

        if (encoding.isPresent()) {
            ajc.args("-encoding", encoding.get());
        }

        if (verbose.getOrElse(false)) {
            ajc.args("-verbose");
        }

        if (showWeaveInfo.getOrElse(false)) {
            ajc.args("-showWeaveInfo");
        }

        if (log.isPresent()) {
            ajc.args("-log", log.get().getAsFile().getAbsolutePath());
        }

        if (progress.getOrElse(false)) {
            ajc.args("-progress");
        }

        if (time.getOrElse(false)) {
            ajc.args("-time");
        }

        if (XterminateAfterCompilation.getOrElse(false)) {
            ajc.args("-XterminateAfterCompilation");
        }

        if (XaddSerialVersionUID.getOrElse(false)) {
            ajc.args("-XaddSerialVersionUID");
        }

        if (XnoInline.getOrElse(false)) {
            ajc.args("-XnoInline");
        }

        if (XserializableAspects.getOrElse(false)) {
            ajc.args("-XserializableAspects");
        }

        if (XnotReweavable.getOrElse(false)) {
            ajc.args("-XnotReweavable");
        }

        if (!files.isEmpty()) {
            ajc.args(files.getFiles());
        }
    }
}
