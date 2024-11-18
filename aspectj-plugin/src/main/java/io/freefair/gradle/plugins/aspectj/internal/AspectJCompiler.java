package io.freefair.gradle.plugins.aspectj.internal;

import io.freefair.gradle.plugins.aspectj.AjcForkOptions;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.gradle.api.internal.tasks.compile.CompilationFailedException;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.WorkResults;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecResult;
import org.gradle.process.JavaExecSpec;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AspectJCompiler implements Compiler<AspectJCompileSpec> {

    private final ExecOperations execOperations;

    @Override
    public WorkResult execute(AspectJCompileSpec spec) {

        ExecResult result = execOperations.javaexec(ajc -> configureJavaExec(ajc, spec));
        if (result.getExitValue() != 0) {
            throw new CompilationFailedException(result.getExitValue());
        }

        return WorkResults.didWork(true);
    }

    @SneakyThrows
    private void configureJavaExec(JavaExecSpec ajc, AspectJCompileSpec spec) {
        if (spec.getLauncher() != null) {
            ajc.setExecutable(spec.getLauncher().getExecutablePath().getAsFile().getAbsolutePath());
        }

        ajc.setWorkingDir(spec.getWorkingDir());
        ajc.setClasspath(spec.getAspectJClasspath());
        ajc.getMainClass().set("org.aspectj.tools.ajc.Main");

        AjcForkOptions forkOptions = spec.getAspectJCompileOptions().getForkOptions();

        ajc.setMinHeapSize(forkOptions.getMemoryInitialSize());
        ajc.setMaxHeapSize(forkOptions.getMemoryMaximumSize());
        ajc.jvmArgs(forkOptions.getJvmArgs());

        List<String> args = new LinkedList<>();

        Collection<File> inpath = new LinkedHashSet<>();

        if (spec.getAdditionalInpath() != null && !spec.getAdditionalInpath().isEmpty()) {
            inpath.addAll(spec.getAdditionalInpath().getFiles());
        }

        if (!spec.getAspectJCompileOptions().getInpath().isEmpty()) {
            inpath.addAll(spec.getAspectJCompileOptions().getInpath().getFiles());
        }

        if (!inpath.isEmpty()) {
            args.add("-inpath");
            args.add(getAsPath(inpath));
        }

        if (!spec.getAspectJCompileOptions().getAspectpath().isEmpty()) {
            args.add("-aspectpath");
            args.add(getAsPath(spec.getAspectJCompileOptions().getAspectpath().getFiles()));
        }

        if (spec.getAspectJCompileOptions().getOutjar().isPresent()) {
            args.add("-outjar");
            args.add(spec.getAspectJCompileOptions().getOutjar().get().getAsFile().getAbsolutePath());
        }

        if (spec.getAspectJCompileOptions().getOutxml().getOrElse(false)) {
            args.add("-outxml");
        }

        if (spec.getAspectJCompileOptions().getOutxmlfile().isPresent()) {
            args.add("-outxmlfile");
            args.add(spec.getAspectJCompileOptions().getOutxmlfile().get());
        }

        if (!spec.getAspectJCompileOptions().getSourceroots().isEmpty()) {
            args.add("-sourceroots");
            args.add(spec.getAspectJCompileOptions().getSourceroots().getAsPath());
        }

        if (spec.getAspectJCompileOptions().getCrossrefs().getOrElse(false)) {
            args.add("-crossrefs");
        }

        List<File> compileClasspath = spec.getCompileClasspath();
        if (compileClasspath != null && !compileClasspath.isEmpty()) {
            args.add("-classpath");
            args.add(getAsPath(compileClasspath));
        }

        if (!spec.getAspectJCompileOptions().getBootclasspath().isEmpty()) {
            args.add("-bootclasspath");
            args.add(getAsPath(spec.getAspectJCompileOptions().getBootclasspath().getFiles()));
        }

        if (!spec.getAspectJCompileOptions().getExtdirs().isEmpty()) {
            args.add("-extdirs");
            args.add(getAsPath(spec.getAspectJCompileOptions().getExtdirs().getFiles()));
        }

        if (spec.getAspectJCompileOptions().getXmlConfigured().isPresent()) {
            args.add("-xmlConfigured");
            args.add(spec.getAspectJCompileOptions().getXmlConfigured().get().getAsFile().getAbsolutePath());
        }

        if (spec.getDestinationDir() != null) {
            args.add("-d");
            args.add(spec.getDestinationDir().getAbsolutePath());
        }

        if (spec.getTargetCompatibility() != null) {
            args.add("-target");
            args.add(spec.getTargetCompatibility());
        }

        if (spec.getSourceCompatibility() != null) {
            args.add("-source");
            args.add(spec.getSourceCompatibility());
        }

        if (spec.getAspectJCompileOptions().getEncoding().isPresent()) {
            args.add("-encoding");
            args.add(spec.getAspectJCompileOptions().getEncoding().get());
        }

        if (spec.getAspectJCompileOptions().getVerbose().getOrElse(false)) {
            args.add("-verbose");
        }

        args.addAll(spec.getAspectJCompileOptions().getCompilerArgs());

        spec.getAspectJCompileOptions().getCompilerArgumentProviders()
                .forEach(commandLineArgumentProvider -> commandLineArgumentProvider.asArguments().forEach(args::add));

        if (spec.getSourceFiles() != null) {
            spec.getSourceFiles().forEach(sourceFile ->
                    args.add(sourceFile.getAbsolutePath())
            );
        }

        File argFile = new File(spec.getTempDir(), "ajc.options");

        Files.write(argFile.toPath(), args, StandardCharsets.UTF_8);

        ajc.args("-argfile", argFile.getAbsolutePath());

        ajc.setIgnoreExitValue(true);
    }

    private String getAsPath(Collection<File> files) {
        return files.stream()
                .filter(File::exists)
                .map(File::getAbsolutePath)
                .collect(Collectors.joining(File.pathSeparator));
    }
}
