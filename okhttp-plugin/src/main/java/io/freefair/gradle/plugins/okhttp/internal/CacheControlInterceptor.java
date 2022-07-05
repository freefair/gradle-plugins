package io.freefair.gradle.plugins.okhttp.internal;

import lombok.RequiredArgsConstructor;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import javax.annotation.Nonnull;
import java.io.IOException;

@RequiredArgsConstructor
public class CacheControlInterceptor implements Interceptor {

    private final CacheControl cacheControl;

    @Nonnull
    @Override
    public Response intercept(@Nonnull Chain chain) throws IOException {
        Request newRequest = chain.request()
                .newBuilder()
                .cacheControl(cacheControl)
                .build();

        return chain.proceed(newRequest);
    }
}
