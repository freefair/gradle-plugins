package io.freefair.gradle.plugins.okhttp.internal;

public interface ProgressListener {
    void readUpdate(long bytesRead, long contentLength);

    void writeUpdate(long bytesWritten, long contentLength);
}
