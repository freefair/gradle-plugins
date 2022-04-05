package io.freefair.gradle.plugins.compress.internal;

import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Lars Grefer
 * @see org.gradle.api.internal.file.archive.compression.GzipArchiver
 */
public class LzmaArchiver extends CommonsCompressArchiver {

    public LzmaArchiver(File xzFile) {
        super(xzFile);
    }

    @Override
    protected LZMACompressorInputStream read(InputStream in) throws IOException {
        return new LZMACompressorInputStream(in);
    }

    @Override
    protected String getSchemePrefix() {
        return "lzma:";
    }
}
