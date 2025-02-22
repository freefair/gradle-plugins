package io.freefair.gradle.plugins.gwt.tasks;

import io.freefair.gradle.plugins.gwt.GwtCompileOptions;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.process.CommandLineArgumentProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lars Grefer
 */
@CacheableTask
public abstract class GwtCompileTask extends AbstractGwtTask implements GwtCompileOptions {

    public GwtCompileTask() {
        getMainClass().set("com.google.gwt.dev.Compiler");

        getArgumentProviders().add(new ArgumentProvider());
    }

    class ArgumentProvider implements CommandLineArgumentProvider {
        @Override
        public Iterable<String> asArguments() {
            List<String> args = new ArrayList<>();


            addStringArg(args, "logLevel", getLogLevel());
            addStringArg(args, "workDir", getWorkDir());
            addBooleanArg(args, "XclosureFormattedOutput", getXClosureFormattedOutput());
            addBooleanArg(args, "compileReport", getCompileReport());
            addBooleanArg(args, "XcheckCasts", getXcheckCasts());
            addBooleanArg(args, "XclassMetadata", getXclassMetadata());
            addBooleanArg(args, "draftCompile", getDraftCompile());
            addBooleanArg(args, "checkAssertions", getCheckAssertions());
            addStringArg(args, "XfragmentCount", getXfragmentCount());
            addStringArg(args, "gen", getGen());
            addBooleanArg(args, "generateJsInteropExports", getGenerateJsInteropExports());
            addListArg(args, "includeJsInteropExports", getIncludeJsInteropExports());
            addListArg(args, "excludeJsInteropExports", getExcludeJsInteropExports());
            addStringArg(args, "XmethodNameDisplayMode", getXmethodNameDisplayMode());
            addStringArg(args, "Xnamespace", getXnamespace());
            addStringArg(args, "optimize", getOptimize());
            addStringArg(args, "saveSource", getSaveSource());
            addMapArg(args, "setProperty", getSetProperty());
            addStringArg(args, "style", getStyle());
            addBooleanArg(args, "failOnError", getFailOnError());
            addBooleanArg(args, "validateOnly", getValidateOnly());
            addStringArg(args, "sourceLevel", getSourceLevel());
            addStringArg(args, "localWorkers", getLocalWorkers());
            addBooleanArg(args, "incremental", getIncremental());
            addStringArg(args, "war", getWar());
            addStringArg(args, "deploy", getDeploy());
            addStringArg(args, "extra", getExtra());
            addStringArg(args, "saveSourceOutput", getSaveSourceOutput());

            return args;
        }
    }
}
