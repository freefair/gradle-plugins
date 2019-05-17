package io.freefair.gradle.plugins;

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
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.language.scala.tasks.AbstractScalaCompile;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

@Getter
@NonNullApi
public class AjcAction implements Action<Task> {

    private final ConfigurableFileCollection classpath;

    private final ConfigurableFileCollection aspectpath;

    private final ListProperty<String> compilerArgs;

    private final Property<Boolean> enabled;

    @Inject
    public AjcAction(ObjectFactory objectFactory) {
        classpath = objectFactory.fileCollection();
        aspectpath = objectFactory.fileCollection();
        compilerArgs = objectFactory.listProperty(String.class);
        enabled = objectFactory.property(Boolean.class).convention(true);
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

        AbstractCompile abstractCompile = (AbstractCompile) task;
        List<String> ajcArgs = new LinkedList<>();
        File argfile = new File(abstractCompile.getTemporaryDir(), "ajc.options");

        ajcArgs.add("-inpath");
        ajcArgs.add(abstractCompile.getDestinationDir().getAbsolutePath());

        if (!aspectpath.isEmpty()) {
            ajcArgs.add("-aspectpath");
            ajcArgs.add(aspectpath.getAsPath());
        }

        if (!abstractCompile.getClasspath().isEmpty()) {
            ajcArgs.add("-classpath");
            ajcArgs.add(abstractCompile.getClasspath().filter(File::exists).getAsPath());
        }

        ajcArgs.add("-d");
        ajcArgs.add(abstractCompile.getDestinationDir().getAbsolutePath());

        ajcArgs.add("-source");
        ajcArgs.add(abstractCompile.getSourceCompatibility());

        ajcArgs.add("-target");
        ajcArgs.add(abstractCompile.getTargetCompatibility());

        CompileOptions compileOptions = findCompileOptions(abstractCompile);
        if (compileOptions != null) {
            String encoding = compileOptions.getEncoding();
            if (encoding != null && !encoding.isEmpty()) {
                ajcArgs.add("-encoding");
                ajcArgs.add(encoding);
            }
            if (compileOptions.isVerbose()) {
                ajcArgs.add("-verbose");
            }
            if (compileOptions.isDeprecation()) {
                ajcArgs.add("-deprecation");
            }
            FileCollection bootstrapClasspath = compileOptions.getBootstrapClasspath();
            if (bootstrapClasspath != null && !bootstrapClasspath.isEmpty()) {
                ajcArgs.add("-bootclasspath");
                ajcArgs.add(bootstrapClasspath.getAsPath());
            }
            String extensionDirs = compileOptions.getExtensionDirs();
            if (extensionDirs != null && !extensionDirs.isEmpty()) {
                ajcArgs.add("-extdirs");
                ajcArgs.add(extensionDirs);
            }
        }

        Files.write(argfile.toPath(), ajcArgs);

        abstractCompile.getProject().javaexec(ajc -> {
            ajc.setClasspath(classpath);
            ajc.setMain("org.aspectj.tools.ajc.Main");

            ajc.args("-argfile", argfile.getAbsolutePath());

            if (compileOptions != null) {
                ajc.setIgnoreExitValue(!compileOptions.isFailOnError());
            }
        });
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
