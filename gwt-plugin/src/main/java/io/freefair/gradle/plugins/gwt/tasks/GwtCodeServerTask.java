package io.freefair.gradle.plugins.gwt.tasks;

import io.freefair.gradle.plugins.gwt.GwtCodeServerOptions;
import org.gradle.process.CommandLineArgumentProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lars Grefer
 */
public abstract class GwtCodeServerTask extends AbstractGwtTask implements GwtCodeServerOptions {

    public GwtCodeServerTask() {
        this.getOutputs().upToDateWhen(task -> false);
        getMainClass().set("com.google.gwt.dev.codeserver.CodeServer");

        getArgumentProviders().add(new ArgProvider());
    }


    class ArgProvider implements CommandLineArgumentProvider {

        @Override
        public Iterable<String> asArguments() {
            List<String> args = new ArrayList<>();

            addBooleanArg(args, "allowMissingSrc", getAllowMissingSrc());
            addBooleanArg(args, "compileTest", getCompileTest());
            addStringArg(args, "compileTestRecompiles", getCompileTestRecompiles());
            addBooleanArg(args, "failOnError", getFailOnError());
            addBooleanArg(args, "precompile", getPrecompile());
            addStringArg(args, "port", getPort());
            addStringArg(args, "src", getSrc());
            addStringArg(args, "workDir", getWorkDir());
            addStringArg(args, "launcherDir", getLauncherDir());
            addStringArg(args, "bindAddress", getBindAddress());
            addStringArg(args, "style", getStyle());
            addMapArg(args, "setProperty", getSetProperty());
            addBooleanArg(args, "incremental", getIncremental());
            addStringArg(args, "sourceLevel", getSourceLevel());
            addStringArg(args, "logLevel", getLogLevel());
            addBooleanArg(args, "generateJsInteropExports", getGenerateJsInteropExports());
            addListArg(args, "includeJsInteropExports", getIncludeJsInteropExports());
            addListArg(args, "excludeJsInteropExports", getExcludeJsInteropExports());
            addStringArg(args, "XmethodNameDisplayMode", getXmethodNameDisplayMode());
            addBooleanArg(args, "XclosureFormattedOutput", getXclosureFormattedOutput());

            return args;
        }
    }

}
