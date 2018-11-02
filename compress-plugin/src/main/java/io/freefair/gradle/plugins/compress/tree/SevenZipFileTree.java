package io.freefair.gradle.plugins.compress.tree;

import io.freefair.gradle.plugins.compress.internal.SevenZipArchiveInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.gradle.api.internal.file.collections.DirectoryFileTreeFactory;
import org.gradle.internal.hash.FileHasher;
import org.gradle.internal.nativeintegration.filesystem.Chmod;

import java.io.File;

public class SevenZipFileTree extends ArchiveFileTree<SevenZipArchiveInputStream, SevenZArchiveEntry> {
    public SevenZipFileTree(File archiveFile, ArchiveInputStreamProvider<SevenZipArchiveInputStream> inputStreamProvider, File tmpDir, Chmod chmod, DirectoryFileTreeFactory directoryFileTreeFactory, FileHasher fileHasher) {
        super(archiveFile, inputStreamProvider, tmpDir, chmod, directoryFileTreeFactory, fileHasher);
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
            if(getArchiveEntry().getHasLastModifiedDate()) {
                return getArchiveEntry().getLastModifiedDate().getTime();
            } else {
                return 0;
            }
        }
    }
}
