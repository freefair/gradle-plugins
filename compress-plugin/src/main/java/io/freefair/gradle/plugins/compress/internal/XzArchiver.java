package io.freefair.gradle.plugins.compress.internal;

import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Lars Grefer
 * @see org.gradle.api.internal.file.archive.compression.GzipArchiver
 */
public class XzArchiver extends CommonsCompressArchiver {

    public XzArchiver(File xzFile) {
        super(xzFile);
    }

    @Override
    protected XZCompressorInputStream read(InputStream in) throws IOException {
        return new XZCompressorInputStream(in, true);
    }

    @Override
    protected String getSchemePrefix() {
        return "xz:";
    }
}
