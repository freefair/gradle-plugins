package io.freefair.gradle.plugins.compress.tree;

import org.apache.commons.compress.archivers.arj.ArjArchiveEntry;
import org.apache.commons.compress.archivers.arj.ArjArchiveInputStream;
import org.gradle.api.internal.file.archive.DecompressionCoordinator;
import org.gradle.api.internal.file.collections.DirectoryFileTreeFactory;
import org.gradle.api.internal.file.temp.TemporaryFileProvider;
import org.gradle.api.provider.Provider;
import org.gradle.internal.file.Chmod;
import org.gradle.internal.hash.FileHasher;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Lars Grefer
 */
public class ArjFileTree extends ArchiveFileTree<ArjArchiveInputStream, ArjArchiveEntry> {

    public ArjFileTree(Provider<File> archiveFile, ArchiveInputStreamProvider<ArjArchiveInputStream> inputStreamProvider, Chmod chmod, DirectoryFileTreeFactory directoryFileTreeFactory, FileHasher fileHasher, DecompressionCoordinator decompressionCoordinator,
                       TemporaryFileProvider temporaryExtractionDir) {
        super(archiveFile, inputStreamProvider, chmod, directoryFileTreeFactory, fileHasher, decompressionCoordinator, temporaryExtractionDir);
    }

    @Override
    ArchiveEntryFileTreeElement createDetails(Chmod chmod, File expandedDir, AtomicBoolean stopFlag) {
        return new ArjArchiveEntryFileTreeElement(chmod, expandedDir, stopFlag);
    }

    public class ArjArchiveEntryFileTreeElement extends ArchiveEntryFileTreeElement {

        ArjArchiveEntryFileTreeElement(Chmod chmod, File expandedDir, AtomicBoolean stopFlag) {
            super(chmod, expandedDir, stopFlag);
        }

        @SuppressWarnings("OctalInteger")
        public int getMode() {
            int unixMode = getArchiveEntry().getUnixMode() & 0777;
            if (unixMode == 0) {
                return super.getMode();
            }
            return unixMode;
        }
    }


}
