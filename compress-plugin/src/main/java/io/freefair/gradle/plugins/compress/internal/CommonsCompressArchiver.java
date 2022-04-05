package io.freefair.gradle.plugins.compress.internal;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.gradle.api.internal.file.archive.compression.CompressedReadableResource;
import org.gradle.api.internal.file.archive.compression.URIBuilder;
import org.gradle.api.resources.ResourceException;
import org.gradle.internal.resource.ResourceExceptions;

import java.io.*;
import java.net.URI;

/**
 * @author Lars Grefer
 * @see org.gradle.api.internal.file.archive.compression.AbstractArchiver
 */
public abstract class CommonsCompressArchiver implements CompressedReadableResource {

    private final File file;
    private final URI uri;

    protected CommonsCompressArchiver(File file) {
        assert file != null;

        this.file = file;
        this.uri = new URIBuilder(file.toURI()).schemePrefix(this.getSchemePrefix()).build();
    }

    @Override
    public File getBackingFile() {
        return file;
    }

    @Override
    public CompressorInputStream read() throws ResourceException {
        InputStream baseFile;
        try {
            baseFile = new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw ResourceExceptions.readMissing(file, e);
        }

        try {
            return read(baseFile);
        } catch (IOException e) {
            IOUtils.closeQuietly(baseFile);
            throw ResourceExceptions.readFailed(file, e);
        }
    }

    protected abstract CompressorInputStream read(InputStream in) throws IOException;

    @Override
    public String getBaseName() {
        return file.getName();
    }

    @Override
    public String getDisplayName() {
        return file.getPath();
    }

    @Override
    public URI getURI() {
        return uri;
    }

    protected abstract String getSchemePrefix();
}
