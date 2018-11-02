package io.freefair.gradle.plugins.compress.tree;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;

import java.io.File;
import java.io.IOException;

@FunctionalInterface
public interface ArchiveInputStreamProvider<IS extends ArchiveInputStream> {
    IS openFile(File file) throws IOException, ArchiveException;
}
