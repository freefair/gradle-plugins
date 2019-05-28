package io.freefair.gradle.plugins.aspectj.internal;

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
import java.nio.file.Paths;
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

        List<String> args = new LinkedList<>();

        if (spec.getDestinationDir() != null) {
            args.add("-d");
            args.add(spec.getDestinationDir().getAbsolutePath());
        }

        if (spec.getSourceCompatibility() != null) {
            args.add("-source");
            args.add(spec.getSourceCompatibility());
        }

        if (spec.getTargetCompatibility() != null) {
            args.add("-target");
            args.add(spec.getTargetCompatibility());
        }

        List<File> compileClasspath = spec.getCompileClasspath();
        if (compileClasspath != null && !compileClasspath.isEmpty()) {
            args.add("-classpath");
            args.add(getAsPath(compileClasspath));
        }

        if (!spec.getAjcCompileOptions().getInpath().isEmpty()) {
            args.add("-inpath");
            args.add(spec.getAjcCompileOptions().getInpath().getAsPath());
        }

        if (!spec.getAjcCompileOptions().getAspectpath().isEmpty()) {
            args.add("-aspectpath");
            args.add(spec.getAjcCompileOptions().getAspectpath().getAsPath());
        }

        if (spec.getAjcCompileOptions().getOutjar().isPresent()) {
            args.add("-outjar");
            args.add(spec.getAjcCompileOptions().getOutjar().get().getAsFile().getAbsolutePath());
        }

        if (spec.getAjcCompileOptions().getOutxml().getOrElse(false)) {
            args.add("-outxml");
        }

        if (spec.getAjcCompileOptions().getOutxmlfile().isPresent()) {
            args.add("-outxmlfile");
            args.add(spec.getAjcCompileOptions().getOutxmlfile().getAsFile().get().getAbsolutePath());
        }

        if (spec.getAjcCompileOptions().getCrossrefs().getOrElse(false)) {
            args.add("-crossrefs");
        }

        if (!spec.getAjcCompileOptions().getSourceroots().isEmpty()) {
            args.add("-sourceroots");
            args.add(spec.getAjcCompileOptions().getSourceroots().getAsPath());
        }

        if (!spec.getAjcCompileOptions().getBootclasspath().isEmpty()) {
            args.add("-bootclasspath");
            args.add(spec.getAjcCompileOptions().getBootclasspath().getAsPath());
        }

        if (!spec.getAjcCompileOptions().getExtdirs().isEmpty()) {
            args.add("-extdirs");
            args.add(spec.getAjcCompileOptions().getExtdirs().getAsPath());
        }

        args.addAll(spec.getAjcCompileOptions().getCompilerArgs());

        spec.getAjcCompileOptions().getCompilerArgumentProviders()
                .forEach(commandLineArgumentProvider -> commandLineArgumentProvider.asArguments().forEach(args::add));

        if (spec.getSourceFiles() != null) {
            spec.getSourceFiles().forEach(sourceFile -> {
                args.add(sourceFile.getAbsolutePath());
            });
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
