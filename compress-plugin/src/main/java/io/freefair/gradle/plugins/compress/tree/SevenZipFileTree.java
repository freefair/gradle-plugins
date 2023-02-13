package io.freefair.gradle.plugins.compress.tree;

import io.freefair.gradle.plugins.compress.internal.SevenZipArchiveInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.gradle.api.internal.file.collections.DirectoryFileTreeFactory;
import org.gradle.api.provider.Provider;
import org.gradle.cache.internal.DecompressionCache;
import org.gradle.internal.file.Chmod;
import org.gradle.internal.hash.FileHasher;

import java.io.File;

public class SevenZipFileTree extends ArchiveFileTree<SevenZipArchiveInputStream, SevenZArchiveEntry> {
    public SevenZipFileTree(Provider<File> archiveFile, ArchiveInputStreamProvider<SevenZipArchiveInputStream> inputStreamProvider, Chmod chmod, DirectoryFileTreeFactory directoryFileTreeFactory, FileHasher fileHasher, DecompressionCache decompressionCache) {
        super(archiveFile, inputStreamProvider, chmod, directoryFileTreeFactory, fileHasher, decompressionCache);
    }

    @Override
    ArchiveEntryFileTreeElement createDetails(Chmod chmod) {
        return new SevenZArchiveEntryFileTreeElement(chmod);
    }

    public class SevenZArchiveEntryFileTreeElement extends ArchiveEntryFileTreeElement {

        SevenZArchiveEntryFileTreeElement(Chmod chmod) {
            super(chmod);
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
