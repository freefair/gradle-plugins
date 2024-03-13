package io.freefair.gradle.plugins.compress.tree;

import io.freefair.gradle.plugins.compress.internal.SevenZipArchiveInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.gradle.api.internal.file.collections.DirectoryFileTreeFactory;
import org.gradle.api.internal.file.temp.TemporaryFileProvider;
import org.gradle.api.provider.Provider;
import org.gradle.cache.internal.DecompressionCoordinator;
import org.gradle.internal.file.Chmod;
import org.gradle.internal.hash.FileHasher;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class SevenZipFileTree extends ArchiveFileTree<SevenZipArchiveInputStream, SevenZArchiveEntry> {
    public SevenZipFileTree(Provider<File> archiveFile, ArchiveInputStreamProvider<SevenZipArchiveInputStream> inputStreamProvider, Chmod chmod, DirectoryFileTreeFactory directoryFileTreeFactory, FileHasher fileHasher, DecompressionCoordinator decompressionCoordinator,
                            TemporaryFileProvider temporaryExtractionDir) {
        super(archiveFile, inputStreamProvider, chmod, directoryFileTreeFactory, fileHasher, decompressionCoordinator, temporaryExtractionDir);
    }

    @Override
    ArchiveEntryFileTreeElement createDetails(Chmod chmod, File expandedDir, AtomicBoolean stopFlag) {
        return new SevenZArchiveEntryFileTreeElement(chmod, expandedDir, stopFlag);
    }

    public class SevenZArchiveEntryFileTreeElement extends ArchiveEntryFileTreeElement {

        protected SevenZArchiveEntryFileTreeElement(Chmod chmod, File expandedDir, AtomicBoolean stopFlag) {
            super(chmod, expandedDir, stopFlag);
        }

        @Override
        public long getLastModified() {
            if (getArchiveEntry().getHasLastModifiedDate()) {
                return getArchiveEntry().getLastModifiedDate().getTime();
            }
            else {
                return 0;
            }
        }
    }
}
