package io.freefair.gradle.plugins.aspectj;

import io.freefair.gradle.plugins.aspectj.internal.AspectJCompileSpec;
import io.freefair.gradle.plugins.aspectj.internal.AspectJCompiler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.gradle.api.Action;
import org.gradle.api.NonNullApi;
import org.gradle.api.Task;
import org.gradle.api.artifacts.transform.CacheableTransform;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.ClasspathNormalizer;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.language.scala.tasks.AbstractScalaCompile;
import org.gradle.process.internal.JavaExecHandleFactory;

import javax.annotation.Nullable;
import javax.imageio.spi.ServiceRegistry;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter
@NonNullApi
public class AjcAction implements Action<Task> {

    private final ConfigurableFileCollection classpath;

    private final ConfigurableFileCollection aspectpath;

    private final ListProperty<String> compilerArgs;

    private final Property<Boolean> enabled;

    @Getter(AccessLevel.NONE)
    private final JavaExecHandleFactory javaExecHandleFactory;

    @Inject
    public AjcAction(ObjectFactory objectFactory, JavaExecHandleFactory javaExecHandleFactory) {
        classpath = objectFactory.fileCollection();
        aspectpath = objectFactory.fileCollection();
        compilerArgs = objectFactory.listProperty(String.class);
        enabled = objectFactory.property(Boolean.class).convention(true);
        this.javaExecHandleFactory = javaExecHandleFactory;
    }


    void addToTask(Task task) {
        task.doLast("ajc", this);
        task.getExtensions().add("ajc", this);

        task.getInputs().files(this.getClasspath())
                .withPropertyName("aspectjClasspath")
                .withNormalizer(ClasspathNormalizer.class)
                .optional(false);

        task.getInputs().files(this.getAspectpath())
                .withPropertyName("aspectpath")
                .withNormalizer(ClasspathNormalizer.class)
                .optional(true);

        task.getInputs().property("ajcArgs", this.getCompilerArgs())
                .optional(true);

        task.getInputs().property("ajcEnabled", this.getEnabled())
                .optional(true);
    }

    @Override
    @SneakyThrows(IOException.class)
    public void execute(Task task) {
        if (!enabled.getOrElse(true)) {
            return;
        }

        AspectJCompileSpec spec = createSpec((AbstractCompile) task);

        new AspectJCompiler(javaExecHandleFactory).execute(spec);
    }

    private AspectJCompileSpec createSpec(AbstractCompile compile) {
        AspectJCompileSpec spec = new AspectJCompileSpec();

        spec.setDestinationDir(compile.getDestinationDir());
        spec.setWorkingDir(compile.getProject().getProjectDir());
        spec.setTempDir(compile.getTemporaryDir());
        spec.setCompileClasspath(new ArrayList<>(compile.getClasspath().filter(File::exists).getFiles()));
        spec.setTargetCompatibility(compile.getTargetCompatibility());
        spec.setSourceCompatibility(compile.getSourceCompatibility());

        spec.setAspectJClasspath(getClasspath());
        spec.setAjcCompileOptions(new AjcCompileOptions(compile.getProject().getObjects()));

        spec.getAjcCompileOptions().getInpath().from(compile.getDestinationDir());
        spec.getAjcCompileOptions().getAspectpath().from(getAspectpath());

        spec.getAjcCompileOptions().setCompilerArgs(getCompilerArgs().get());

        return spec;
    }

    @Nullable
    private CompileOptions findCompileOptions(AbstractCompile abstractCompile) {
        if (abstractCompile instanceof JavaCompile) {
            return ((JavaCompile) abstractCompile).getOptions();
        }
        if (abstractCompile instanceof GroovyCompile) {
            return ((GroovyCompile) abstractCompile).getOptions();
        }
        if (abstractCompile instanceof AbstractScalaCompile) {
            return ((AbstractScalaCompile) abstractCompile).getOptions();
        }
        return null;
    }
}
