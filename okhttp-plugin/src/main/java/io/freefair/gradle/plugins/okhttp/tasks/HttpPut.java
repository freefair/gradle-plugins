package io.freefair.gradle.plugins.okhttp.tasks;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

import javax.annotation.Nullable;

/**
 * @author Lars Grefer
 */
public abstract class HttpPut extends OkHttpRequestTask {

    @Input
    @Optional
    public abstract Property<String> getContentType();

    @Override
    public Request.Builder buildRequest(Request.Builder builder) {
        return super.buildRequest(builder)
                .put(getRequestBody(getContentType().map(MediaType::get).getOrNull()));
    }

    public abstract RequestBody getRequestBody(@Nullable MediaType contentType);
}
