package io.freefair.gradle.plugins.okhttp.tasks;

import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

import javax.annotation.Nullable;

/**
 * @author Lars Grefer
 */
@Getter
public abstract class HttpPut extends OkHttpRequestTask {

    @Input
    private final Property<String> contentType = getProject().getObjects().property(String.class);

    @Override
    public Request.Builder buildRequest(Request.Builder builder) {
        return super.buildRequest(builder)
                .put(getRequestBody(contentType.map(MediaType::get).getOrNull()));
    }

    public abstract RequestBody getRequestBody(@Nullable MediaType contentType);
}
