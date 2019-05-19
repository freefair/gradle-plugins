package io.freefair.gradle.plugins.github.internal;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface GithubService {

    default Call<Repo> getRepository(String slug) {
        int i = slug.indexOf("/");

        return getRepository(slug.substring(0, i), slug.substring(i + 1));
    }

    @GET("repos/{owner}/{repo}")
    Call<Repo> getRepository(@Path("owner") String owner, @Path("repo") String repo);

    @GET("users/{user}")
    Call<User> getUser(@Path("user") String user);

    @GET
    Call<License> getLicense(@Url String url);
}
