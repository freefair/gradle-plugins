package io.freefair.gradle.plugins.jacoco.tasks;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.testing.jacoco.tasks.JacocoBase;
import org.gradle.workers.WorkerExecutor;
import org.jacoco.core.tools.ExecDumpClient;
import org.jacoco.core.tools.ExecFileLoader;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

/**
 * @author Lars Grefer
 * @see org.jacoco.ant.DumpTask
 */
@Getter
public class JacocoDump extends JacocoBase {

    @Internal
    private final WorkerExecutor workerExecutor;

    @Input
    private final Property<Boolean> dump = getProject().getObjects().property(Boolean.class).convention(true);

    @Input
    private final Property<Boolean> reset = getProject().getObjects().property(Boolean.class).convention(false);

    @Input
    private final Property<Integer> retryCount = getProject().getObjects().property(Integer.class).convention(10);

    @Input
    private final Property<String> address = getProject().getObjects().property(String.class);

    @Input
    private final Property<Integer> port = getProject().getObjects().property(Integer.class).convention(6300);

    @OutputFile
    private final RegularFileProperty destfile = getProject().getObjects().fileProperty();

    @Input
    private final Property<Boolean> append = getProject().getObjects().property(Boolean.class).convention(true);

    @Inject
    public JacocoDump(WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor;
    }

    @TaskAction
    public void dump() {
        workerExecutor.submit(Action.class, workerConfiguration -> {
            workerConfiguration.setClasspath(getJacocoClasspath());
            workerConfiguration.params(dump.get(), reset.get(), retryCount.get());
            workerConfiguration.params(address.get(), port.get());
            workerConfiguration.params(destfile.getAsFile().get(), append.get());
        });
    }

    @RequiredArgsConstructor
    private static class Action implements Runnable {

        private final boolean dump;
        private final boolean reset;
        private final int retryCount;

        private final String address;
        private final int port;

        private final File destfile;
        private final boolean append;

        @Override
        public void run() {
            ExecDumpClient client = new ExecDumpClient();

            client.setDump(dump);
            client.setReset(reset);
            client.setRetryCount(retryCount);

            try {
                ExecFileLoader loader = client.dump(address, port);
                if (dump) {
                    loader.save(destfile, append);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
