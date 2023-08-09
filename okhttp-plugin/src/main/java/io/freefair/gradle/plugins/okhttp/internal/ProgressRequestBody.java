package io.freefair.gradle.plugins.okhttp.internal;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

class ProgressRequestBody extends RequestBody {

    private final RequestBody requestBody;
    private final ProgressListener progressListener;

    ProgressRequestBody(RequestBody requestBody, ProgressListener progressListener) {
        this.requestBody = requestBody;
        this.progressListener = progressListener;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(@Nonnull BufferedSink bufferedSink) throws IOException {
        requestBody.writeTo(Okio.buffer(sink(bufferedSink)));
    }

    @Override
    public boolean isDuplex() {
        return requestBody.isDuplex();
    }

    @Override
    public boolean isOneShot() {
        return requestBody.isOneShot();
    }

    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            long totalBytesWritten = 0L;

            @Override
            public void write(@Nonnull Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                totalBytesWritten += byteCount;
                progressListener.writeUpdate(totalBytesWritten, requestBody.contentLength());
            }
        };
    }
}
