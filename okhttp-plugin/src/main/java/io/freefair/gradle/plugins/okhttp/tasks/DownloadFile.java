package io.freefair.gradle.plugins.okhttp.tasks;

import okhttp3.Response;
import okio.Okio;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;

import java.io.IOException;

/**
 * @author Lars Grefer
 */
public class DownloadFile extends HttpGet {

    @OutputFile
    private RegularFileProperty outputFile = getProject().getObjects().fileProperty();

    @Override
    public void handleResponse(Response response) throws IOException {
        super.handleResponse(response);

        Okio.buffer(Okio.sink(outputFile.getAsFile().get()))
                .writeAll(response.body().source());
    }
}
