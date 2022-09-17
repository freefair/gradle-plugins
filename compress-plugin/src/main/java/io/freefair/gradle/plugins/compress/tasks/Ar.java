package io.freefair.gradle.plugins.compress.tasks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
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

/**
 * @author Lars Grefer
 */
@Getter
@Setter
@Slf4j
public abstract class Ar extends AbstractArchiveTask {

    @Input
    private final Property<Integer> longFileMode = getProject().getObjects().property(Integer.class);

    public Ar() {
        getArchiveExtension().convention("ar");
        getLongFileMode().convention(ArArchiveOutputStream.LONGFILE_ERROR);
    }

    public void setLongFileMode(String longFileMode) {
        if (longFileMode.equalsIgnoreCase("LONGFILE_ERROR") || longFileMode.equalsIgnoreCase("ERROR")) {
            getLongFileMode().set(ArArchiveOutputStream.LONGFILE_ERROR);
        } else if (longFileMode.equalsIgnoreCase("LONGFILE_BSD") || longFileMode.equalsIgnoreCase("BSD")) {
            getLongFileMode().set(ArArchiveOutputStream.LONGFILE_BSD);
        } else {
            throw new IllegalArgumentException("Expected 'LONGFILE_ERROR'/'ERROR' or 'LONGFILE_BSD'/'BSD', but was '" + longFileMode + "'");
        }
    }

    @Override
    protected CopyAction createCopyAction() {
        return this::execute;
    }

    @SneakyThrows
    private WorkResult execute(CopyActionProcessingStream copyActionProcessingStream) {
        try (ArArchiveOutputStream archiveOutputStream = new ArArchiveOutputStream(new FileOutputStream(getArchiveFile().get().getAsFile()))) {

            archiveOutputStream.setLongFileMode(getLongFileMode().get());

            copyActionProcessingStream.process(new StreamAction(archiveOutputStream));

            return WorkResults.didWork(true);
        }
    }

    @RequiredArgsConstructor
    private static class StreamAction implements CopyActionProcessingStreamAction {
        private final ArArchiveOutputStream outputFile;

        @Override
        @SneakyThrows
        public void processFile(FileCopyDetailsInternal details) {
            if (details.isDirectory()) {
                return;
            }

            ArArchiveEntry archiveEntry = new ArArchiveEntry(
                    details.getPath(),
                    details.getSize(),
                    0,
                    0,
                    33188,
                    details.getLastModified() / 1000
            );

            try {
                outputFile.putArchiveEntry(archiveEntry);
            } catch (IOException e) {
                log.error(e.getMessage());
                throw e;
            }

            details.copyTo(outputFile);

            outputFile.closeArchiveEntry();
        }
    }
}
