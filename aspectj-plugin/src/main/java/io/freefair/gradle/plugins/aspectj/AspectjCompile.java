package io.freefair.gradle.plugins.aspectj;

import io.freefair.gradle.plugins.aspectj.internal.AspectJCompileSpec;
import io.freefair.gradle.plugins.aspectj.internal.AspectJCompiler;
import lombok.AccessLevel;
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
public class AspectjCompile extends AbstractCompile {

    @Getter(AccessLevel.NONE)
    private final FileSystemOperations fileSystemOperations;
    @Getter(AccessLevel.NONE)
    private final ProjectLayout projectLayout;

    @Classpath
    private final ConfigurableFileCollection aspectjClasspath = getProject().getObjects().fileCollection();

    @Nested
    private final CompileOptions options = getProject().getObjects().newInstance(CompileOptions.class);

    @Nested
    private final AspectJCompileOptions ajcOptions = getProject().getObjects().newInstance(AspectJCompileOptions.class);

    @Nested
    @Optional
    private final Property<JavaLauncher> launcher = getProject().getObjects().property(JavaLauncher.class);

    @Inject
    public AspectjCompile(FileSystemOperations fileSystemOperations, ProjectLayout projectLayout) {
        this.fileSystemOperations = fileSystemOperations;
        this.projectLayout = projectLayout;
    }

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

    @Override
    @CompileClasspath
    public FileCollection getClasspath() {
        return super.getClasspath();
    }

    @TaskAction
    protected void compile() {
        fileSystemOperations.delete(spec -> spec.delete(getDestinationDirectory()).setFollowSymlinks(false));

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
        spec.setDestinationDir(getDestinationDirectory().getAsFile().get());
        spec.setWorkingDir(projectLayout.getProjectDirectory().getAsFile());
        spec.setTempDir(getTemporaryDir());
        spec.setCompileClasspath(new ArrayList<>(getClasspath().getFiles()));
        spec.setSourceCompatibility(getSourceCompatibility());
        spec.setTargetCompatibility(getTargetCompatibility());
        spec.setAspectJClasspath(getAspectjClasspath());
        spec.setAspectJCompileOptions(getAjcOptions());
        spec.setLauncher(launcher.getOrNull());

        return spec;
    }

    public void options(Action<CompileOptions> action) {
        action.execute(getOptions());
    }

    public void ajcOptions(Action<AspectJCompileOptions> action) {
        action.execute(getAjcOptions());
    }

}
