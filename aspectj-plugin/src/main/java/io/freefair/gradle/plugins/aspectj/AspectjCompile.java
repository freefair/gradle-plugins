package io.freefair.gradle.plugins.aspectj;

import io.freefair.gradle.plugins.aspectj.internal.AspectJCompileSpec;
import io.freefair.gradle.plugins.aspectj.internal.AspectJCompiler;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.file.*;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.process.internal.JavaExecHandleFactory;

import javax.inject.Inject;
import java.util.ArrayList;

@Getter
@CacheableTask
public abstract class AspectjCompile extends AbstractCompile {

    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();

    @Inject
    protected abstract ProjectLayout getProjectLayout();

    @Classpath
    public abstract ConfigurableFileCollection getAspectjClasspath();

    @Nested
    private final CompileOptions options = getProject().getObjects().newInstance(CompileOptions.class);

    @Nested
    private final AspectJCompileOptions ajcOptions = getProject().getObjects().newInstance(AspectJCompileOptions.class);

    @Nested
    @Optional
    public abstract Property<JavaLauncher> getLauncher();

    /**
     * {@inheritDoc}
     */
    @Override
    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.RELATIVE)
    public FileTree getSource() {
        return super.getSource();
    }

    /**
     * Make the destinationDirectory optional. outjar could also be used.
     */
    @Override
    @Optional
    @OutputDirectory
    public DirectoryProperty getDestinationDirectory() {
        return super.getDestinationDirectory();
    }

    @Override
    @CompileClasspath
    public FileCollection getClasspath() {
        return super.getClasspath();
    }

    @TaskAction
    protected void compile() {
        if (!getDestinationDirectory().isPresent() && !getAjcOptions().getOutjar().isPresent()) {
            throw new IllegalStateException("Neither destinationDirectory, nor outjar are set.");
        }

        if (getDestinationDirectory().isPresent()) {
            getFileSystemOperations().delete(spec -> spec.delete(getDestinationDirectory()).setFollowSymlinks(false));
        }

        AspectJCompileSpec spec = createSpec();
        WorkResult result = getCompiler().execute(spec);
        setDidWork(result.getDidWork());
    }

    private AspectJCompiler getCompiler() {
        return new AspectJCompiler(getServices().get(JavaExecHandleFactory.class));
    }

    protected AspectJCompileSpec createSpec() {
        AspectJCompileSpec spec = new AspectJCompileSpec();
        spec.setSourceFiles(getSource());
        if (getDestinationDirectory().isPresent()) {
            spec.setDestinationDir(getDestinationDirectory().getAsFile().get());
        }
        spec.setWorkingDir(getProjectLayout().getProjectDirectory().getAsFile());
        spec.setTempDir(getTemporaryDir());
        spec.setCompileClasspath(new ArrayList<>(getClasspath().getFiles()));
        spec.setSourceCompatibility(getSourceCompatibility());
        spec.setTargetCompatibility(getTargetCompatibility());
        spec.setAspectJClasspath(getAspectjClasspath());
        spec.setAspectJCompileOptions(getAjcOptions());
        spec.setLauncher(getLauncher().getOrNull());

        return spec;
    }

    public void options(Action<CompileOptions> action) {
        action.execute(getOptions());
    }

    public void ajcOptions(Action<AspectJCompileOptions> action) {
        action.execute(getAjcOptions());
    }

}
