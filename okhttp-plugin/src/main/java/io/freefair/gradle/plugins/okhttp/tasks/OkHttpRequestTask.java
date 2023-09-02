package io.freefair.gradle.plugins.okhttp.tasks;

import io.freefair.gradle.plugins.okhttp.internal.ProgressInterceptor;
import io.freefair.gradle.plugins.okhttp.internal.ProgressListener;
import lombok.RequiredArgsConstructor;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.gradle.api.GradleException;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Duration;

/**
 * Base class for tasks which execute exactly one {@link Request HTTP Request}.
 *
 * @author Lars Grefer
 */
public abstract class OkHttpRequestTask extends OkHttpTask {

    @Inject
    protected abstract ProgressLoggerFactory getProgressLoggerFactory();

    @Input
    public abstract Property<String> getUrl();

    @Input
    public abstract MapProperty<String, String> getHeaders();

    @Input
    @Optional
    public abstract Property<String> getUsername();

    @Input
    @Optional
    public abstract Property<String> getPassword();

    @TaskAction
    public void executeRequest() throws IOException {

        ProgressLogger progressLogger = getProgressLoggerFactory().newOperation(OkHttpRequestTask.class);

        Request request = buildRequest(new Request.Builder()).build();

        OkHttpClient client = getOkHttpClient()
                .newBuilder()
                .addNetworkInterceptor(new ProgressInterceptor(new GradleProcessListener(progressLogger)))
                .build();

        progressLogger.start(request.toString(), "Call");

        try (Response response = client.newCall(request).execute()) {
            handleResponse(response);
        } finally {
            progressLogger.completed();
        }

    }

    public Request.Builder buildRequest(Request.Builder builder) {

        getHeaders().get().forEach(builder::header);

        if (getUsername().isPresent() && getPassword().isPresent()) {
            builder.header("Authorization", Credentials.basic(getUsername().get(), getPassword().get()));
        }

        if (getUrl().isPresent()) {
            builder.url(getUrl().get());
        }

        return builder;
    }

    public void handleResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            getLogger().error("{}: {}", response.code(), response.message());
            getLogger().error(response.body().string());
            throw new GradleException(response.message());
        }
    }

    @RequiredArgsConstructor
    private static class GradleProcessListener implements ProgressListener {

        private final ProgressLogger progressLogger;

        private long uploadStart;
        private long downloadStart;

        @Override
        public void readUpdate(long bytesRead, long contentLength) {
            if (downloadStart == 0) {
                downloadStart = System.nanoTime();
            }
            update("Download", bytesRead, contentLength, downloadStart);
        }

        @Override
        public void writeUpdate(long bytesWritten, long contentLength) {
            if (uploadStart == 0) {
                uploadStart = System.nanoTime();
            }
            update("Upload", bytesWritten, contentLength, uploadStart);
        }

        private void update(String prefix, long bytes, long contentLength, long startNanos) {
            Duration currentDuration = Duration.ofNanos(System.nanoTime() - startNanos);

            double bps = (double) bytes / currentDuration.toMillis() * 1000;

            if (contentLength < 1) {
                progressLogger.progress(String.format("%s %s [%s/s]", prefix, makeHumanReadable(bytes), makeHumanReadable(bps)));
            }
            else {
                double percent = (double) bytes / contentLength * 100d;
                progressLogger.progress(String.format("%s %.2f%% (%s / %s) [%s/s]", prefix, percent, makeHumanReadable(bytes), makeHumanReadable(contentLength), makeHumanReadable(bps)));
            }
        }

        private String makeHumanReadable(long bytes) {

            if (bytes < 1024) {
                return String.format("%d B", bytes);
            }

            double kib = bytes / 1024d;
            if (kib < 1024) {
                return String.format("%.2f KiB", kib);
            }

            double mib = kib / 1024d;
            if (mib < 1024) {
                return String.format("%.2f MiB", mib);
            }

            double gib = mib / 1024d;
            if (gib < 1024) {
                return String.format("%.2f GiB", gib);
            }

            double tib = gib / 1024d;
            if (tib < 1024) {
                return String.format("%.2f TiB", tib);
            }

            double pib = tib / 1024d;
            return String.format("%.2f PiB", pib);
        }
    }
}
