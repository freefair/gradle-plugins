package io.freefair.gradle.plugins.compress.tree;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.internal.file.archive.DecompressionCoordinator;
import org.gradle.api.internal.file.archive.ZipFileTree;
import org.gradle.api.internal.file.collections.DirectoryFileTree;
import org.gradle.api.internal.file.collections.DirectoryFileTreeFactory;
import org.gradle.api.internal.file.temp.TemporaryFileProvider;
import org.gradle.api.provider.Provider;
import org.gradle.internal.file.Chmod;
import org.gradle.internal.hash.FileHasher;
import org.gradle.internal.hash.HashCode;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Lars Grefer
 * @see ZipFileTree
 */
public class ArchiveFileTree<IS extends ArchiveInputStream, E extends ArchiveEntry> extends AbstractArchiveFileTree {

    private final Provider<File> fileProvider;
    private final ArchiveInputStreamProvider<IS> inputStreamProvider;
    private final Chmod chmod;
    private final DirectoryFileTreeFactory directoryFileTreeFactory;
    private final FileHasher fileHasher;
    private final TemporaryFileProvider temporaryExtractionDir;

    public ArchiveFileTree(
            Provider<File> zipFile,
            ArchiveInputStreamProvider<IS> inputStreamProvider,
            Chmod chmod,
            DirectoryFileTreeFactory directoryFileTreeFactory,
            FileHasher fileHasher,
            DecompressionCoordinator decompressionCoordinator,
            TemporaryFileProvider temporaryExtractionDir
    ) {
        super(decompressionCoordinator);
        this.fileProvider = zipFile;
        this.inputStreamProvider = inputStreamProvider;
        this.chmod = chmod;
        this.directoryFileTreeFactory = directoryFileTreeFactory;
        this.fileHasher = fileHasher;
        this.temporaryExtractionDir = temporaryExtractionDir;
    }

    ArchiveEntryFileTreeElement createDetails(Chmod chmod, File expandedDir, AtomicBoolean stopFlag) {
        return new ArchiveEntryFileTreeElement(chmod, expandedDir, stopFlag);
    }

    public DirectoryFileTree getMirror() {
        return directoryFileTreeFactory.create(getExpandedDir());
    }

    @Override
    public void visit(FileVisitor visitor) {
        File archiveFile = fileProvider.get();
        if (!archiveFile.exists()) {
            throw new InvalidUserDataException(String.format("Cannot expand %s as it does not exist.", getDisplayName()));
        }
        if (!archiveFile.isFile()) {
            throw new InvalidUserDataException(String.format("Cannot expand %s as it is not a file.", getDisplayName()));
        }

        File expandedDir = getExpandedDir();
        decompressionCoordinator.exclusiveAccessTo(expandedDir, () -> {
            AtomicBoolean stopFlag = new AtomicBoolean();

            try {
                IS archiveInputStream = inputStreamProvider.openFile(archiveFile);
                try {
                    ArchiveEntry archiveEntry;

                    while (!stopFlag.get() && (archiveEntry = archiveInputStream.getNextEntry()) != null) {
                        ArchiveEntryFileTreeElement details = createDetails(chmod, expandedDir, stopFlag);
                        details.archiveInputStream = archiveInputStream;
                        details.archiveEntry = (E) archiveEntry;

                        try {
                            if (archiveEntry.isDirectory()) {
                                visitor.visitDir(details);
                            }
                            else {
                                visitor.visitFile(details);
                            }
                        } finally {
                            details.close();
                        }
                    }
                } finally {
                    archiveInputStream.close();
                }
            } catch (Exception e) {
                throw new GradleException(String.format("Could not expand %s.", getDisplayName()), e);
            }
        });
    }

    @Override
    public String getDisplayName() {
        return String.format("archive '%s'", fileProvider.getOrNull());
    }

    private File getExpandedDir() {
        File archiveFile = fileProvider.get();
        HashCode fileHash = hashFile(archiveFile);
        String expandedDirName = "archive_" + fileHash;
        return temporaryExtractionDir.newTemporaryDirectory(".cache", "expanded", expandedDirName);
    }

    private HashCode hashFile(File tarFile) {
        try {
            return fileHasher.hash(tarFile);
        } catch (Exception e) {
            throw cannotExpand(e);
        }
    }

    private RuntimeException cannotExpand(Exception e) {
        throw new InvalidUserDataException(String.format("Cannot expand %s.", getDisplayName()), e);
    }

    @Override
    protected Provider<File> getBackingFileProvider() {
        return fileProvider;
    }

    @Getter
    class ArchiveEntryFileTreeElement extends AbstractArchiveFileTreeElement implements FileVisitDetails, Closeable {

        private IS archiveInputStream;
        private E archiveEntry;
        private boolean closed;
        private boolean inputStreamUsed;

        /**
         * Creates a new instance.
         *
         * @param chmod       the chmod instance to use
         * @param expandedDir the directory to extract the archived file to
         * @param stopFlag    the stop flag to use
         */
        protected ArchiveEntryFileTreeElement(Chmod chmod, File expandedDir, AtomicBoolean stopFlag) {
            super(chmod, expandedDir, stopFlag);
        }

        @Override
        protected String getEntryName() {
            return archiveEntry.getName();
        }

        @Override
        public String getDisplayName() {
            return String.format("%s %s!%s", archiveEntry.getClass().getSimpleName(), fileProvider.get(), archiveEntry.getName());
        }

        @Override
        @SneakyThrows(IOException.class)
        public InputStream open() {
            if (!closed && !inputStreamUsed) {
                // We are still visiting this FTE, so we can use the overall ArchiveInputStream
                return new ArchiveEntryInputStream(this);
            }
            // We already used the the overall ArchiveInputStream or it has moved to another entry
            // so we have to open a new InputStream

            // If getFile() was called before, we can use the file to open a new InputStream.
            if (file != null && file.exists()) {
                return new FileInputStream(file);
            }

            // As last resort: Reopen the Archive and skip forward to our ArchiveEntry
            try {
                IS archiveInputStream = inputStreamProvider.openFile(fileProvider.get());

                ArchiveEntry tmp;

                while ((tmp = archiveInputStream.getNextEntry()) != null) {
                    if (tmp.equals(archiveEntry)) {
                        return archiveInputStream;
                    }
                }

                throw new IOException(archiveEntry.getName() + " not found in " + fileProvider.get());

            } catch (ArchiveException e) {
                throw new IOException(e);
            }
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    /**
     * Wrapper around an {@link ArchiveInputStream} for reading one {@link ArchiveEntry}.
     */
    class ArchiveEntryInputStream extends FilterInputStream {

        private final ArchiveEntryFileTreeElement fileTreeElement;

        ArchiveEntryInputStream(ArchiveEntryFileTreeElement fileTreeElement) {
            super(fileTreeElement.archiveInputStream);
            this.fileTreeElement = fileTreeElement;
        }

        @Override
        public int read() throws IOException {
            checkEntryNotClosed();
            fileTreeElement.inputStreamUsed = true;
            return super.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            checkEntryNotClosed();
            fileTreeElement.inputStreamUsed = true;
            return super.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            checkEntryNotClosed();
            fileTreeElement.inputStreamUsed = true;
            return super.read(b, off, len);
        }

        private void checkEntryNotClosed() throws IOException {
            if (fileTreeElement.closed) {
                throw new IOException("The underlying FileTreeElement is no longer visited");
            }
        }

        @Override
        public void close() {
        }
    }
}
