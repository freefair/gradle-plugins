package io.freefair.gradle.plugins.jacoco.tasks;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.InvalidUserDataException;
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

    /**
     * Sets whether execution data should be downloaded from the remote host.
     * Defaults to <code>true</code>
     *
     * @see org.jacoco.ant.DumpTask#setDump(boolean)
     */
    @Input
    private final Property<Boolean> dump = getProject().getObjects().property(Boolean.class).convention(true);

    /**
     * Sets whether a reset command should be sent after the execution data has been dumped.
     * Defaults to <code>false</code>
     *
     * @see org.jacoco.ant.DumpTask#setReset(boolean)
     */
    @Input
    private final Property<Boolean> reset = getProject().getObjects().property(Boolean.class).convention(false);

    /**
     * Number of retries which the goal will attempt to establish a connection.
     * This can be used to wait until the target JVM is successfully launched.
     *
     * @see org.jacoco.ant.DumpTask#setRetryCount(int)
     */
    @Input
    private final Property<Integer> retryCount = getProject().getObjects().property(Integer.class).convention(10);

    /**
     * IP Address or hostname to connect to.
     * Defaults to <code>localhost</code>
     *
     * @see org.jacoco.ant.DumpTask#setAddress(String)
     */
    @Input
    private final Property<String> address = getProject().getObjects().property(String.class).convention("localhost");

    /**
     * Port number to connect to.
     * Default is <code>6300</code>
     *
     * @see org.jacoco.ant.DumpTask#setPort(int)
     */
    @Input
    private final Property<Integer> port = getProject().getObjects().property(Integer.class).convention(6300);

    /**
     * Sets the location of the execution data file to write. This parameter is
     * required when dump is <code>true</code>.
     *
     * @see org.jacoco.ant.DumpTask#setDestfile(File)
     */
    @OutputFile
    private final RegularFileProperty destfile = getProject().getObjects().fileProperty();

    /**
     * <code>true</code> if the destination file it to be appended to.
     * <code>false</code> if the file is to be overwritten
     *
     * @see org.jacoco.ant.DumpTask#setAppend(boolean)
     */
    @Input
    private final Property<Boolean> append = getProject().getObjects().property(Boolean.class).convention(true);

    @Inject
    public JacocoDump(WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor;
    }

    @TaskAction
    public void dump() {

        if (port.getOrElse(6300) <= 0) {
            throw new InvalidUserDataException("Invalid port value");
        }

        if (dump.getOrElse(true) && !destfile.isPresent()) {
            throw new InvalidUserDataException("Destination file is required when dumping execution data");
        }

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
