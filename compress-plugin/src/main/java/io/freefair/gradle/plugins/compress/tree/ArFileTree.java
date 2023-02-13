package io.freefair.gradle.plugins.compress.tree;

import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.gradle.api.internal.file.collections.DirectoryFileTreeFactory;
import org.gradle.api.provider.Provider;
import org.gradle.cache.internal.DecompressionCache;
import org.gradle.internal.file.Chmod;
import org.gradle.internal.hash.FileHasher;

import java.io.File;

/**
 * @author Lars Grefer
 */
public class ArFileTree extends ArchiveFileTree<ArArchiveInputStream, ArArchiveEntry> {

    public ArFileTree(Provider<File> archiveFile, ArchiveInputStreamProvider<ArArchiveInputStream> inputStreamProvider, Chmod chmod, DirectoryFileTreeFactory directoryFileTreeFactory, FileHasher fileHasher, DecompressionCache decompressionCache) {
        super(archiveFile, inputStreamProvider, chmod, directoryFileTreeFactory, fileHasher, decompressionCache);
    }

    @Override
    ArchiveEntryFileTreeElement createDetails(Chmod chmod) {
        return new ArArchiveEntryFileTreeElement(chmod);
    }

    public class ArArchiveEntryFileTreeElement extends ArchiveEntryFileTreeElement {

        ArArchiveEntryFileTreeElement(Chmod chmod) {
            super(chmod);
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
