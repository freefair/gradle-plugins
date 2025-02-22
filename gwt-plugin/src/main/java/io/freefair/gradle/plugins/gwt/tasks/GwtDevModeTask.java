package io.freefair.gradle.plugins.gwt.tasks;

import io.freefair.gradle.plugins.gwt.GwtDevModeOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lars Grefer
 */
public abstract class GwtDevModeTask extends AbstractGwtTask implements GwtDevModeOptions {

    public GwtDevModeTask() {
        this.getOutputs().upToDateWhen(task -> false);

        getMainClass().convention("com.google.gwt.dev.DevMode");

        getArgumentProviders().add(new ArgumentProvider());
    }

    class ArgumentProvider implements org.gradle.process.CommandLineArgumentProvider {
        @Override
        public Iterable<String> asArguments() {
            List<String> args = new ArrayList<>();

            addBooleanArg(args, "startServer", getStartServer());
            addStringArg(args, "port", getPort());
            addStringArg(args, "logdir", getLogdir());
            addStringArg(args, "logLevel", getLogLevel());
            addStringArg(args, "gen", getGen());
            addStringArg(args, "bindAddress", getBindAddress());
            addStringArg(args, "codeServerPort", getCodeServerPort());
            addBooleanArg(args, "superDevMode", getSuperDevMode());
            addStringArg(args, "server", getServer());
            addStringArg(args, "startupUrl", getStartupUrl());
            addStringArg(args, "war", getWar());
            addStringArg(args, "deploy", getDeploy());
            addStringArg(args, "extra", getExtra());
            addStringArg(args, "modulePathPrefix", getModulePathPrefix());
            addStringArg(args, "workDir", getWorkDir());
            addStringArg(args, "XmethodNameDisplayMode", getXmethodNameDisplayMode());
            addStringArg(args, "sourceLevel", getSourceLevel());
            addBooleanArg(args, "generateJsInteropExports", getGenerateJsInteropExports());
            addListArg(args, "includeJsInteropExports", getIncludeJsInteropExports());
            addListArg(args, "excludeJsInteropExports", getExcludeJsInteropExports());
            addBooleanArg(args, "incremental", getIncremental());
            addStringArg(args, "style", getStyle());
            addBooleanArg(args, "failOnError", getFailOnError());
            addMapArg(args, "setProperty", getSetProperty());

            return args;
        }
    }
}
