package io.freefair.gradle.plugins.compress.tree;

import org.apache.commons.compress.archivers.dump.DumpArchiveEntry;
import org.apache.commons.compress.archivers.dump.DumpArchiveInputStream;
import org.gradle.api.internal.file.collections.DirectoryFileTreeFactory;
import org.gradle.api.provider.Provider;
import org.gradle.cache.internal.DecompressionCache;
import org.gradle.internal.file.Chmod;
import org.gradle.internal.hash.FileHasher;

import java.io.File;

/**
 * @author Lars Grefer
 */
public class DumpFileTree extends ArchiveFileTree<DumpArchiveInputStream, DumpArchiveEntry> {

    public DumpFileTree(Provider<File> archiveFile, ArchiveInputStreamProvider<DumpArchiveInputStream> inputStreamProvider, Chmod chmod, DirectoryFileTreeFactory directoryFileTreeFactory, FileHasher fileHasher, DecompressionCache decompressionCache) {
        super(archiveFile, inputStreamProvider, chmod, directoryFileTreeFactory, fileHasher, decompressionCache);
    }

    @Override
    ArchiveEntryFileTreeElement createDetails(Chmod chmod) {
        return new DumpArchiveEntryFileTreeElement(chmod);
    }

    public class DumpArchiveEntryFileTreeElement extends ArchiveEntryFileTreeElement {

        DumpArchiveEntryFileTreeElement(Chmod chmod) {
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
