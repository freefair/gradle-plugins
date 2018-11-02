package io.freefair.gradle.plugins.compress.tasks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZMethod;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.gradle.api.internal.file.CopyActionProcessingStreamAction;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.internal.file.copy.CopyActionProcessingStream;
import org.gradle.api.internal.file.copy.FileCopyDetailsInternal;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.WorkResults;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Lars Grefer
 */
@Getter
@Setter
public class SevenZip extends AbstractArchiveTask {

    @Input
    private final Property<SevenZMethod> contentCompression = getProject().getObjects().property(SevenZMethod.class);

    public SevenZip() {
        setExtension("7z");
        contentCompression.set(SevenZMethod.LZMA2);
    }

    @Override
    protected CopyAction createCopyAction() {
        return this::execute;
    }

    @SneakyThrows
    private WorkResult execute(CopyActionProcessingStream stream) {
        try (SevenZOutputFile outputFile = new SevenZOutputFile(getArchivePath())) {

            if (contentCompression.isPresent()) {
                outputFile.setContentCompression(contentCompression.get());
            }

            stream.process(new StreamAction(outputFile));

            outputFile.finish();

            return WorkResults.didWork(true);
        }
    }

    @RequiredArgsConstructor
    private static class StreamAction implements CopyActionProcessingStreamAction {
        private final SevenZOutputFile outputFile;

        @Override
        @SneakyThrows
        public void processFile(FileCopyDetailsInternal details) {
            SevenZArchiveEntry archiveEntry = new SevenZArchiveEntry();
            archiveEntry.setLastModifiedDate(details.getLastModified());
            archiveEntry.setName(details.getPath());

            if (details.isDirectory()) {
                archiveEntry.setDirectory(true);

                outputFile.putArchiveEntry(archiveEntry);

            } else {
                archiveEntry.setDirectory(false);

                outputFile.putArchiveEntry(archiveEntry);

                details.copyTo(new OutputStream() {
                    @Override
                    public void write(int b) throws IOException {
                        outputFile.write(b);
                    }

                    @Override
                    public void write(byte[] b) throws IOException {
                        outputFile.write(b);
                    }

                    @Override
                    public void write(byte[] b, int off, int len) throws IOException {
                        outputFile.write(b, off, len);
                    }
                });
            }
            outputFile.closeArchiveEntry();
        }
    }
}
