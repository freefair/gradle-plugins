package io.freefair.gradle.plugins.okhttp.tasks;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;

import javax.annotation.Nullable;

/**
 * @author Lars Grefer
 */
public abstract class UploadFile extends HttpPut {

    @InputFile
    public abstract RegularFileProperty getFile();

    @Override
    public RequestBody getRequestBody(@Nullable MediaType contentType) {
        return RequestBody.create(getFile().getAsFile().get(), contentType);
    }
}
