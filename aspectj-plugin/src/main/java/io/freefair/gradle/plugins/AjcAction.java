package io.freefair.gradle.plugins;

import lombok.Getter;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.api.Action;
import org.gradle.api.NonNullApi;
import org.gradle.api.Task;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.language.scala.tasks.AbstractScalaCompile;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

@Getter
@NonNullApi
public class AjcAction implements Action<Task> {

    private final ConfigurableFileCollection classpath;

    private final ConfigurableFileCollection aspectpath;

    private final ListProperty<String> compilerArgs;

    private final Property<Boolean> enabled;

    @Inject
    public AjcAction(ObjectFactory providerFactory, ProjectLayout projectLayout) {
        classpath = projectLayout.configurableFiles();
        aspectpath = projectLayout.configurableFiles();
        compilerArgs = providerFactory.listProperty(String.class);
        enabled = providerFactory.property(Boolean.class);
    }

    @Override
    public void execute(Task task) {
        AbstractCompile abstractCompile = (AbstractCompile) task;

        if (!enabled.getOrElse(true)) {
            return;
        }

        abstractCompile.getProject().javaexec(ajc -> {
            ajc.setClasspath(classpath);
            ajc.setMain("org.aspectj.tools.ajc.Main");

            ajc.args("-inpath", abstractCompile.getDestinationDir());
            if (!aspectpath.isEmpty()) {
                ajc.args("-aspectpath", aspectpath.getAsPath());
            }
            if (!abstractCompile.getClasspath().isEmpty()) {

                String classpath = abstractCompile.getClasspath().getAsPath();

                if (System.getProperty("os.name").toLowerCase().contains("windows") && classpath.length() > 10_000) {
                    File file = new File(task.getTemporaryDir(), "ajc/classpath.arg");

                    try {
                        ResourceGroovyMethods.setText(file, "-classpath " + classpath);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }

                    ajc.args("-argfile", file);
                } else {
                    ajc.args("-classpath", classpath);
                }
            }
            ajc.args("-d", abstractCompile.getDestinationDir());
            ajc.args("-source", abstractCompile.getSourceCompatibility());
            ajc.args("-target", abstractCompile.getTargetCompatibility());

            CompileOptions compileOptions = findCompileOptions(abstractCompile);

            if (compileOptions != null) {
                String encoding = compileOptions.getEncoding();
                if (encoding != null && !encoding.isEmpty()) {
                    ajc.args("-encoding", encoding);
                }
                if (compileOptions.isVerbose()) {
                    ajc.args("-verbose");
                }
                if (compileOptions.isDeprecation()) {
                    ajc.args("-deprecation");
                }
                FileCollection bootstrapClasspath = compileOptions.getBootstrapClasspath();
                if (bootstrapClasspath != null && !bootstrapClasspath.isEmpty()) {
                    ajc.args("-bootclasspath", bootstrapClasspath.getAsPath());
                }
                String extensionDirs = compileOptions.getExtensionDirs();
                if (extensionDirs != null && !extensionDirs.isEmpty()) {
                    ajc.args("-extdirs", extensionDirs);
                }

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
