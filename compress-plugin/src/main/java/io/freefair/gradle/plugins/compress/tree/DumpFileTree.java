package io.freefair.gradle.plugins.compress.tree;

import org.apache.commons.compress.archivers.dump.DumpArchiveEntry;
import org.apache.commons.compress.archivers.dump.DumpArchiveInputStream;
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
public class DumpFileTree extends ArchiveFileTree<DumpArchiveInputStream, DumpArchiveEntry> {

    public DumpFileTree(Provider<File> archiveFile, ArchiveInputStreamProvider<DumpArchiveInputStream> inputStreamProvider, Chmod chmod, DirectoryFileTreeFactory directoryFileTreeFactory, FileHasher fileHasher, DecompressionCoordinator decompressionCoordinator,
                        TemporaryFileProvider temporaryExtractionDir) {
        super(archiveFile, inputStreamProvider, chmod, directoryFileTreeFactory, fileHasher, decompressionCoordinator, temporaryExtractionDir);
    }

    @Override
    ArchiveEntryFileTreeElement createDetails(Chmod chmod, File expandedDir, AtomicBoolean stopFlag) {
        return new DumpArchiveEntryFileTreeElement(chmod, expandedDir, stopFlag);
    }

    public class DumpArchiveEntryFileTreeElement extends ArchiveEntryFileTreeElement {

        DumpArchiveEntryFileTreeElement(Chmod chmod, File expandedDir, AtomicBoolean stopFlag) {
            super(chmod, expandedDir, stopFlag);
        }

        @SuppressWarnings("OctalInteger")
        public int getMode() {
            int unixMode = getArchiveEntry().getMode() & 0777;
            if (unixMode == 0) {
                return super.getMode();
            }
            return unixMode;
        }
    }


}
