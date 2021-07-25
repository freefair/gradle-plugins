package io.freefair.gradle.plugins.compress.tree;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.file.RelativePath;
import org.gradle.api.internal.file.AbstractFileTreeElement;
import org.gradle.api.internal.file.archive.AbstractArchiveFileTree;
import org.gradle.api.internal.file.archive.ZipFileTree;
import org.gradle.api.internal.file.collections.DirectoryFileTree;
import org.gradle.api.internal.file.collections.DirectoryFileTreeFactory;
import org.gradle.api.provider.Provider;
import org.gradle.internal.file.Chmod;
import org.gradle.internal.hash.FileHasher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Lars Grefer
 * @see ZipFileTree
 */
@RequiredArgsConstructor
public class ArchiveFileTree<IS extends ArchiveInputStream, E extends ArchiveEntry> extends AbstractArchiveFileTree {

    private final File archiveFile;
    private final ArchiveInputStreamProvider<IS> inputStreamProvider;
    private final File tmpDir;
    private final Chmod chmod;
    private final DirectoryFileTreeFactory directoryFileTreeFactory;
    private final FileHasher fileHasher;

    ArchiveEntryFileTreeElement createDetails(Chmod chmod) {
        return new ArchiveEntryFileTreeElement(chmod);
    }

    public DirectoryFileTree getMirror() {
        return directoryFileTreeFactory.create(getExpandedDir());
    }

    @Override
    public void visit(FileVisitor visitor) {
        if (!archiveFile.exists()) {
            throw new InvalidUserDataException(String.format("Cannot expand %s as it does not exist.", getDisplayName()));
        }
        if (!archiveFile.isFile()) {
            throw new InvalidUserDataException(String.format("Cannot expand %s as it is not a file.", getDisplayName()));
        }

        AtomicBoolean stopFlag = new AtomicBoolean();

        try {
            IS archiveInputStream = inputStreamProvider.openFile(archiveFile);
            try {
                ArchiveEntry archiveEntry;

                while (!stopFlag.get() && (archiveEntry = archiveInputStream.getNextEntry()) != null) {
                    ArchiveEntryFileTreeElement details = createDetails(chmod);
                    details.archiveInputStream = archiveInputStream;
                    details.archiveEntry = (E) archiveEntry;
                    details.stopFlag = stopFlag;

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
    }

    @Override
    public String getDisplayName() {
        return String.format("archive '%s'", archiveFile);
    }

    private File getExpandedDir() {
        String expandedDirName = archiveFile.getName() + "_" + fileHasher.hash(archiveFile);
        return new File(tmpDir, expandedDirName);
    }

    @Override
    protected Provider<File> getBackingFileProvider() {
        return null;
    }

    @Getter
    class ArchiveEntryFileTreeElement extends AbstractFileTreeElement implements FileVisitDetails, Closeable {

        private IS archiveInputStream;
        private E archiveEntry;
        private AtomicBoolean stopFlag;
        @Nullable
        private File file;
        private boolean closed;
        private boolean inputStreamUsed;

        ArchiveEntryFileTreeElement(Chmod chmod) {
            super(chmod);
        }

        @Override
        public void stopVisiting() {
            stopFlag.set(true);
        }

        @Override
        public String getDisplayName() {
            return String.format("%s %s!%s", archiveEntry.getClass().getSimpleName(), archiveFile, archiveEntry.getName());
        }

        @Override
        public long getLastModified() {
            return archiveEntry.getLastModifiedDate().getTime();
        }

        @Override
        public long getSize() {
            return archiveEntry.getSize();
        }

        @Override
        public String getName() {
            return archiveEntry.getName();
        }

        @Override
        public boolean isDirectory() {
            return archiveEntry.isDirectory();
        }

        @Override
        public RelativePath getRelativePath() {
            return new RelativePath(!archiveEntry.isDirectory(), archiveEntry.getName().split("/"));
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
                IS archiveInputStream = inputStreamProvider.openFile(archiveFile);

                ArchiveEntry tmp;

                while ((tmp = archiveInputStream.getNextEntry()) != null) {
                    if (tmp.equals(archiveEntry)) {
                        return archiveInputStream;
                    }
                }

                throw new IOException(archiveEntry.getName() + " not found in " + archiveFile);

            } catch (ArchiveException e) {
                throw new IOException(e);
            }
        }

        /**
         * @see ZipFileTree.DetailsImpl#getFile()
         */
        @Nonnull
        public File getFile() {
            if (file == null) {
                file = new File(getExpandedDir(), archiveEntry.getName());
                if (!file.exists()) {
                    copyTo(file);
                }
            }
            return file;
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
