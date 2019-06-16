package io.freefair.gradle.plugins.okhttp.tasks;

import lombok.Getter;
import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;

import javax.annotation.Nullable;

/**
 * @author Lars Grefer
 */
@Getter
@Setter
public class UploadFile extends HttpPut {

    @InputFile
    private final RegularFileProperty file = getProject().getObjects().fileProperty();

    @Override
    public RequestBody getRequestBody(@Nullable MediaType contentType) {
        return RequestBody.create(contentType, file.getAsFile().get());
    }
}
