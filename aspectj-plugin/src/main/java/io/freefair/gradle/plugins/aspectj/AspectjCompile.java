package io.freefair.gradle.plugins.aspectj;

import io.freefair.gradle.plugins.aspectj.internal.AspectJCompileSpec;
import io.freefair.gradle.plugins.aspectj.internal.AspectJCompiler;
import lombok.Getter;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.process.internal.JavaExecHandleFactory;

import java.util.ArrayList;

@Getter
@CacheableTask
public class AspectjCompile extends AbstractCompile {

    @Classpath
    private final ConfigurableFileCollection aspectjClasspath = getProject().getObjects().fileCollection();

    @Nested
    private final CompileOptions options = getProject().getObjects().newInstance(CompileOptions.class);

    @Nested
    private final AspectJCompileOptions ajcOptions = getProject().getObjects().newInstance(AspectJCompileOptions.class);

    @Override
    @TaskAction
    protected void compile() {
        getProject().delete(getDestinationDir());

        AspectJCompileSpec spec = createSpec();
        WorkResult result = getCompiler().execute(spec);
        setDidWork(result.getDidWork());
    }

    private AspectJCompiler getCompiler() {
        return new AspectJCompiler(getServices().get(JavaExecHandleFactory.class));
    }

    private AspectJCompileSpec createSpec() {
        AspectJCompileSpec spec = new AspectJCompileSpec();
        spec.setSourceFiles(getSource());
        spec.setDestinationDir(getDestinationDir());
        spec.setWorkingDir(getProject().getProjectDir());
        spec.setTempDir(getTemporaryDir());
        spec.setCompileClasspath(new ArrayList<>(getClasspath().getFiles()));
        spec.setSourceCompatibility(getSourceCompatibility());
        spec.setTargetCompatibility(getTargetCompatibility());
        spec.setAspectJClasspath(getAspectjClasspath());
        spec.setAspectJCompileOptions(getAjcOptions());

        return spec;
    }

}
