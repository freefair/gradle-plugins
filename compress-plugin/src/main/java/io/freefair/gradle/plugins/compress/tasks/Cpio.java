package io.freefair.gradle.plugins.compress.tasks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream;
import org.apache.commons.compress.archivers.cpio.CpioConstants;
import org.apache.commons.compress.utils.CharsetNames;
import org.gradle.api.internal.file.CopyActionProcessingStreamAction;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.internal.file.copy.CopyActionProcessingStream;
import org.gradle.api.internal.file.copy.FileCopyDetailsInternal;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.WorkResults;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Lars Grefer
 */
@Getter
@Setter
@Slf4j
public abstract class Cpio extends AbstractArchiveTask {

    @Input
    private final Property<Short> format = getProject().getObjects().property(Short.class);

    @Input
    public abstract Property<Integer> getBlockSize();

    @Input
    public abstract Property<String> getEncoding();

    public Cpio() {
        getArchiveExtension().convention("cpio");
        getFormat().convention(CpioConstants.FORMAT_NEW);
        getBlockSize().convention(CpioConstants.BLOCK_SIZE);
        getEncoding().convention(StandardCharsets.US_ASCII.name());
    }

    public void setFormat(String format) {
        switch (format.toUpperCase()) {
            case "NEW":
            case "FORMAT_NEW":
                getFormat().set(CpioConstants.FORMAT_NEW);
                return;
            case "NEW_CRC":
            case "FORMAT_NEW_CRC":
                getFormat().set(CpioConstants.FORMAT_NEW_CRC);
                return;
            case "OLD_ASCII":
            case "FORMAT_OLD_ASCII":
                getFormat().set(CpioConstants.FORMAT_OLD_ASCII);
                return;
            case "OLD_BINARY":
            case "FORMAT_OLD_BINARY":
                getFormat().set(CpioConstants.FORMAT_OLD_BINARY);
                return;
            default:
                throw new IllegalArgumentException("Unknown format: " + format);
        }
    }

    @Override
    protected CopyAction createCopyAction() {
        return this::execute;
    }

    @SneakyThrows
    private WorkResult execute(CopyActionProcessingStream copyActionProcessingStream) {
        try (CpioArchiveOutputStream archiveOutputStream = new CpioArchiveOutputStream(
                new FileOutputStream(getArchiveFile().get().getAsFile()),
                getFormat().get(),
                getBlockSize().get(),
                getEncoding().get()
        )) {
            copyActionProcessingStream.process(new StreamAction(archiveOutputStream, getFormat().get()));

            return WorkResults.didWork(true);
        }
    }

    @RequiredArgsConstructor
    private static class StreamAction implements CopyActionProcessingStreamAction {
        private final CpioArchiveOutputStream outputFile;
        private final short format;

        @Override
        @SneakyThrows
        public void processFile(FileCopyDetailsInternal details) {
            CpioArchiveEntry archiveEntry = new CpioArchiveEntry(format, details.getPath());

            archiveEntry.setTime(details.getLastModified());

            if (details.isDirectory()) {
                archiveEntry.setMode(CpioConstants.C_ISDIR);
            } else {
                archiveEntry.setMode(CpioConstants.C_ISREG);
                archiveEntry.setSize(details.getSize());
            }

            try {
                outputFile.putArchiveEntry(archiveEntry);
            } catch (IOException e) {
                log.error(e.getMessage());
                throw e;
            }

            if (!details.isDirectory()) {
                details.copyTo(outputFile);
            }

            outputFile.closeArchiveEntry();
        }
    }
}
