package io.freefair.gradle.plugins.github.internal;

import io.freefair.gradle.plugins.github.GithubExtension;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.gradle.api.NonNullApi;
import org.gradle.api.provider.Property;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.File;
import java.io.IOException;

@Slf4j
@NonNullApi
public class GithubClient {

    private Retrofit retrofit;

    public GithubClient(GithubExtension githubExtension, OkHttpClient okHttpClient) {

        OkHttpClient client = okHttpClient.newBuilder()
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Property<String> username = githubExtension.getUsername();
                    Property<String> token = githubExtension.getToken();
                    if (username.isPresent() && token.isPresent()) {
                        request = request.newBuilder()
                                .header("Authorization", Credentials.basic(username.get(), token.get()))
                                .build();
                    }
                    return chain.proceed(request);
                })
                .addInterceptor(this::logRateLimit)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private Response logRateLimit(Interceptor.Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());

        String limitString = response.header("X-RateLimit-Limit");
        String remainingString = response.header("X-RateLimit-Remaining");

        if (limitString != null && remainingString != null) {
            int limit = Integer.parseInt(limitString);
            int remaining = Integer.parseInt(remainingString);

            double d = remaining / (double) limit;

            if (d < 0.1) {
                log.warn("{}/{} GitHub requests remaining", remainingString, limitString);
            }
            else {
                log.info("{}/{} GitHub requests remaining", remainingString, limitString);
            }
        }

        return response;
    }

    public GithubService getGithubService() {
        return retrofit.create(GithubService.class);
    }
}
