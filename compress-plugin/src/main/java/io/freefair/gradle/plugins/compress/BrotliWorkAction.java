package io.freefair.gradle.plugins.compress;

import lombok.extern.slf4j.Slf4j;
import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkAction;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.freefair.gradle.plugins.compress.tasks.CompressorWorkAction.checkMinCompressionRatio;

@Deprecated
@Slf4j
public abstract class BrotliWorkAction implements WorkAction<BrotliWorkParameters> {

    @Inject
    public abstract ExecOperations getExecOperations();

    @Override
    public void execute() {

        File outputFile = getParameters().getTargetFile().getAsFile().get();
        File parentFile = outputFile.getParentFile();

        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        List<String> command = new ArrayList<>();
        command.add("brotli");

        if (getParameters().getForce().getOrElse(true)) {
            command.add("--force");
        }

        if (getParameters().getQuality().isPresent()) {
            command.add("--quality=" + getParameters().getQuality().get());
        }

        if (getParameters().getBest().getOrElse(false)) {
            command.add("--best");
        }

        command.add("--output=" + outputFile.getAbsolutePath());
        command.add(getParameters().getSourceFile().get().getAsFile().getAbsolutePath());

        getExecOperations().exec(spec -> {
            spec.commandLine(command);
            spec.setIgnoreExitValue(false);
        });

        checkMinCompressionRatio(getParameters());

    }
}
