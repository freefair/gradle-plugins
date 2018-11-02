package io.freefair.gradle.plugins.compress.internal;

import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.IOException;

@RequiredArgsConstructor
public class SevenZipArchiveInputStream extends ArchiveInputStream {

    private final SevenZFile sevenZFile;

    @Override
    public ArchiveEntry getNextEntry() throws IOException {
        return sevenZFile.getNextEntry();
    }

    @Override
    public int read() throws IOException {
        return sevenZFile.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return sevenZFile.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return sevenZFile.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        sevenZFile.close();
    }
}
