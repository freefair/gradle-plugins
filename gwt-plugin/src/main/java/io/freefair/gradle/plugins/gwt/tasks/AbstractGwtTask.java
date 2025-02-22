package io.freefair.gradle.plugins.gwt.tasks;

import io.freefair.gradle.plugins.gwt.CommonGwtToolOptions;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.JavaExec;

import java.util.Collection;

/**
 * @author Lars Grefer
 */
public abstract class AbstractGwtTask extends JavaExec implements CommonGwtToolOptions {

    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getGwtClasspath();

    public AbstractGwtTask() {
        classpath(getGwtClasspath());
    }

    @Override
    public void exec() {
        args(getModule().get());
        super.exec();
    }


    static void addStringArg(Collection<String> args, String argName, DirectoryProperty property) {

        if (property.isPresent()) {
            args.add("-" + argName);
            args.add(property.get().getAsFile().getAbsolutePath());
        }
    }

    static void addMapArg(Collection<String> args, String argName, MapProperty<String, String> property) {

        if (property.isPresent()) {
            property.get().forEach((key, value) -> {
                args.add("-" + argName);
                args.add(key + "=" + value);
            });
        }
    }

    static void addListArg(Collection<String> args, String argName, ListProperty<String> property) {

        if (property.isPresent()) {
            property.get().forEach(key -> {
                args.add("-" + argName);
                args.add(key);
            });
        }
    }

    static void addStringArg(Collection<String> args, String argName, Provider<?> property) {

        if (property.isPresent()) {
            args.add("-" + argName);
            args.add(property.get().toString());
        }
    }

    static void addBooleanArg(Collection<String> args, String argName, Provider<Boolean> property) {

        if (property.isPresent()) {
            if (property.get()) {
                args.add("-" + argName);
            }
            else {
                if (argName.startsWith("X")) {
                    args.add("-Xno" + argName.substring(1));
                }
                else {
                    args.add("-no" + argName);
                }
            }

        }
    }
}
