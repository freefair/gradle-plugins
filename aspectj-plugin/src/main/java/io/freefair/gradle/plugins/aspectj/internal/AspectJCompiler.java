package io.freefair.gradle.plugins.aspectj.internal;

import io.freefair.gradle.plugins.aspectj.AjcForkOptions;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.gradle.api.internal.tasks.compile.CompilationFailedException;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.WorkResults;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.ExecHandle;
import org.gradle.process.internal.JavaExecHandleBuilder;
import org.gradle.process.internal.JavaExecHandleFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AspectJCompiler implements Compiler<AspectJCompileSpec> {

    private final JavaExecHandleFactory javaExecHandleFactory;

    @Override
    public WorkResult execute(AspectJCompileSpec spec) {

        ExecHandle handle = createCompilerHandle(spec);
        executeCompiler(handle);

        return WorkResults.didWork(true);
    }

    @SneakyThrows
    private ExecHandle createCompilerHandle(AspectJCompileSpec spec) {
        JavaExecHandleBuilder ajc = javaExecHandleFactory.newJavaExec();
        ajc.setWorkingDir(spec.getWorkingDir());
        ajc.setClasspath(spec.getAspectJClasspath());
        ajc.setMain("org.aspectj.tools.ajc.Main");

        AjcForkOptions forkOptions = spec.getAspectJCompileOptions().getForkOptions();

        ajc.setMinHeapSize(forkOptions.getMemoryInitialSize());
        ajc.setMaxHeapSize(forkOptions.getMemoryMaximumSize());
        ajc.jvmArgs(forkOptions.getJvmArgs());

        List<String> args = new LinkedList<>();

        if (!spec.getAspectJCompileOptions().getInpath().isEmpty()) {
            args.add("-inpath");
            args.add(spec.getAspectJCompileOptions().getInpath().getAsPath());
        }

        if (!spec.getAspectJCompileOptions().getAspectpath().isEmpty()) {
            args.add("-aspectpath");
            args.add(spec.getAspectJCompileOptions().getAspectpath().getAsPath());
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
            args.add(spec.getAspectJCompileOptions().getOutxmlfile().getAsFile().get().getAbsolutePath());
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
            args.add(spec.getAspectJCompileOptions().getBootclasspath().getAsPath());
        }

        if (!spec.getAspectJCompileOptions().getExtdirs().isEmpty()) {
            args.add("-extdirs");
            args.add(spec.getAspectJCompileOptions().getExtdirs().getAsPath());
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

        Files.write(argFile.toPath(), args);

        ajc.args("-argfile", argFile.getAbsolutePath());

        ajc.setIgnoreExitValue(true);
        return ajc.build();
    }

    private void executeCompiler(ExecHandle handle) {
        handle.start();
        ExecResult result = handle.waitForFinish();
        if (result.getExitValue() != 0) {
            throw new CompilationFailedException(result.getExitValue());
        }
    }

    private String getAsPath(List<File> files) {
        return files.stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.joining(File.pathSeparator));
    }
}
