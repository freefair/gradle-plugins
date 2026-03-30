package io.freefair.gradle.plugins.okhttp.tasks;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.work.DisableCachingByDefault;

import javax.annotation.Nullable;

/**
 * @author Lars Grefer
 */
@DisableCachingByDefault(because = "Remote state cannot be tracked")
public abstract class UploadFile extends HttpPut {

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getFile();

    @Override
    public RequestBody getRequestBody(@Nullable MediaType contentType) {
        return RequestBody.create(getFile().getAsFile().get(), contentType);
    }
}
