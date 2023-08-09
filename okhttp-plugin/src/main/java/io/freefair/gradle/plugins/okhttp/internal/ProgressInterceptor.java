package io.freefair.gradle.plugins.okhttp.internal;

import lombok.RequiredArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @see <a href="https://github.com/square/okhttp/blob/master/samples/guide/src/main/java/okhttp3/recipes/Progress.java">https://github.com/square/okhttp/blob/master/samples/guide/src/main/java/okhttp3/recipes/Progress.java</a>
 */
@RequiredArgsConstructor
public class ProgressInterceptor implements Interceptor {

    private final ProgressListener progressListener;

    @SuppressWarnings({"resource", "KotlinInternalInJava"})
    @NotNull
    @Override
    public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        Request request = chain.request();

        RequestBody requestBody = request.body();
        String requestMethod = request.method();

        if (requestBody != null) {
            ProgressRequestBody newBody = new ProgressRequestBody(requestBody, progressListener);
            request = request.newBuilder().method(requestMethod, newBody).build();
        }

        Response originalResponse = chain.proceed(request);
        return originalResponse.newBuilder()
                .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                .build();
    }

}
