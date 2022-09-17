package io.freefair.gradle.plugins.okhttp.tasks;

import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;

import java.io.IOException;

/**
 * @author Lars Grefer
 */
public abstract class DownloadFile extends HttpGet {

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @Override
    public void handleResponse(Response response) throws IOException {
        super.handleResponse(response);

        try (BufferedSink sink = Okio.buffer(Okio.sink(getOutputFile().getAsFile().get()))) {
            sink.writeAll(response.body().source());
        }
    }
}
