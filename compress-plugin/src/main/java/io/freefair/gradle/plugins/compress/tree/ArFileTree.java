package io.freefair.gradle.plugins.compress.tree;

import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
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
public class ArFileTree extends ArchiveFileTree<ArArchiveInputStream, ArArchiveEntry> {

    public ArFileTree(Provider<File> archiveFile, ArchiveInputStreamProvider<ArArchiveInputStream> inputStreamProvider, Chmod chmod, DirectoryFileTreeFactory directoryFileTreeFactory, FileHasher fileHasher, DecompressionCoordinator decompressionCoordinator,
                      TemporaryFileProvider temporaryExtractionDir) {
        super(archiveFile, inputStreamProvider, chmod, directoryFileTreeFactory, fileHasher, decompressionCoordinator, temporaryExtractionDir);
    }

    @Override
    ArchiveEntryFileTreeElement createDetails(Chmod chmod, File expandedDir, AtomicBoolean stopFlag) {
        return new ArArchiveEntryFileTreeElement(chmod, expandedDir, stopFlag);
    }

    public class ArArchiveEntryFileTreeElement extends ArchiveEntryFileTreeElement {

        ArArchiveEntryFileTreeElement(Chmod chmod, File expandedDir, AtomicBoolean stopFlag) {
            super(chmod, expandedDir, stopFlag);
        }

    }


}
