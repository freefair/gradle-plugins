package io.freefair.gradle.plugins.aspectj;

import io.freefair.gradle.plugins.aspectj.internal.AspectJCompileSpec;
import io.freefair.gradle.plugins.aspectj.internal.AspectJCompiler;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.NonNullApi;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.ClasspathNormalizer;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.process.internal.JavaExecHandleFactory;
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile;

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import static io.freefair.gradle.util.TaskUtils.*;

/**
 * @author Lars Grefer
 * @see AspectJPostCompileWeavingPlugin
 */
@Getter
@NonNullApi
public class AjcAction implements Action<Task> {

    @Getter(AccessLevel.NONE)
    private final ProjectLayout projectLayout;
    @Getter(AccessLevel.NONE)
    private final JavaExecHandleFactory javaExecHandleFactory;

    private final ConfigurableFileCollection classpath;

    private final Property<Boolean> enabled;

    private final AspectJCompileOptions options;

    private final ConfigurableFileCollection additionalInpath;

    @Nested
    @Optional
    private final Property<JavaLauncher> launcher;

    public void options(Action<AspectJCompileOptions> action) {
        action.execute(getOptions());
    }

    @Inject
    public AjcAction(ProjectLayout projectLayout, ObjectFactory objectFactory, JavaExecHandleFactory javaExecHandleFactory) {
        this.projectLayout = projectLayout;
        options = new AspectJCompileOptions(objectFactory);
        classpath = objectFactory.fileCollection();
        additionalInpath = objectFactory.fileCollection();

        launcher = objectFactory.property(JavaLauncher.class);

        enabled = objectFactory.property(Boolean.class).convention(true);
        this.javaExecHandleFactory = javaExecHandleFactory;
    }

    @SuppressWarnings("WeakerAccess")
    public void addToTask(Task task) {
        task.doLast("ajc", this);
        task.getExtensions().add("ajc", this);

        task.getInputs().files(this.getClasspath())
                .withPropertyName("aspectjClasspath")
                .withNormalizer(ClasspathNormalizer.class)
                .optional(false);

        task.getInputs().property("ajcEnabled", this.getEnabled())
                .optional(true);

        try {
            registerNested(task, AspectJCompileOptions.class, this.getOptions(), "ajcOptions");
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void execute(Task task) {
        if (!enabled.getOrElse(true)) {
            return;
        }

        AspectJCompileSpec spec = createSpec(task);

        new AspectJCompiler(javaExecHandleFactory).execute(spec);
    }

    private AspectJCompileSpec createSpec(Task compile) {
        AspectJCompileSpec spec = new AspectJCompileSpec();

        if (compile instanceof AbstractCompile) {
            AbstractCompile abstractCompile = (AbstractCompile) compile;
            spec.setDestinationDir(abstractCompile.getDestinationDirectory().get().getAsFile());
            spec.setCompileClasspath(new ArrayList<>(abstractCompile.getClasspath().filter(File::exists).getFiles()));
            spec.setTargetCompatibility(abstractCompile.getTargetCompatibility());
            spec.setSourceCompatibility(abstractCompile.getSourceCompatibility());
        }
        else if (compile instanceof KotlinJvmCompile) {
            KotlinJvmCompile kotlinJvmCompile = (KotlinJvmCompile) compile;
            spec.setDestinationDir(kotlinJvmCompile.getDestinationDirectory().get().getAsFile());
            spec.setCompileClasspath(new ArrayList<>(kotlinJvmCompile.getLibraries().filter(File::exists).getFiles()));
            spec.setTargetCompatibility(kotlinJvmCompile.getKotlinOptions().getJvmTarget());
        }

        spec.setWorkingDir(projectLayout.getProjectDirectory().getAsFile());
        spec.setTempDir(compile.getTemporaryDir());

        spec.setAspectJClasspath(getClasspath());
        spec.setAspectJCompileOptions(getOptions());
        spec.setAdditionalInpath(getAdditionalInpath());

        spec.setLauncher(launcher.getOrNull());

        return spec;
    }
}
