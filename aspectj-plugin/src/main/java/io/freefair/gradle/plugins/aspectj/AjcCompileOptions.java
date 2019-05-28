package io.freefair.gradle.plugins.aspectj;

import lombok.Data;
import lombok.Getter;
import org.graalvm.compiler.options.Option;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.compile.AbstractOptions;
import org.gradle.process.CommandLineArgumentProvider;

import java.util.ArrayList;
import java.util.List;

@Data
public class AjcCompileOptions extends AbstractOptions {

    @InputFiles
    @SkipWhenEmpty
    private final ConfigurableFileCollection inpath;

    @Classpath
    private final ConfigurableFileCollection aspectpath;

    @OutputFile
    @Optional
    private final RegularFileProperty outjar;

    @Input
    private final Property<Boolean> outxml;

    @OutputFile
    @Optional
    private final RegularFileProperty outxmlfile;

    @SkipWhenEmpty
    @InputFiles
    private final ConfigurableFileCollection sourceroots;

    @Input
    private final Property<Boolean> crossrefs;

    @Classpath
    private final ConfigurableFileCollection bootclasspath;

    @Classpath
    private final ConfigurableFileCollection extdirs;

    private List<String> compilerArgs = new ArrayList<>();
    private List<CommandLineArgumentProvider> compilerArgumentProviders = new ArrayList<>();

    public AjcCompileOptions(ObjectFactory objectFactory) {
        inpath = objectFactory.fileCollection();
        aspectpath = objectFactory.fileCollection();
        outjar = objectFactory.fileProperty();
        outxml = objectFactory.property(Boolean.class).convention(false);
        outxmlfile = objectFactory.fileProperty();
        sourceroots = objectFactory.fileCollection();
        crossrefs = objectFactory.property(Boolean.class).convention(false);
        bootclasspath = objectFactory.fileCollection();
        extdirs = objectFactory.fileCollection();
    }
}
