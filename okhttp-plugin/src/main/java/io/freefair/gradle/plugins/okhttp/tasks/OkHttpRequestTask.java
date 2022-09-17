package io.freefair.gradle.plugins.okhttp.tasks;

import io.freefair.gradle.plugins.okhttp.OkHttpPlugin;
import okhttp3.*;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

/**
 * @author Lars Grefer
 */
public abstract class OkHttpRequestTask extends DefaultTask {

    @Input
    public abstract Property<String> getUrl();

    @Input
    public abstract MapProperty<String, String> getHeaders();

    @Input
    @Optional
    public abstract Property<String> getUsername();

    @Input
    @Optional
    public abstract Property<String> getPassword();

    @TaskAction
    public void executeRequest() throws IOException {

        Request request = buildRequest(new Request.Builder()).build();

        OkHttpClient client = getOkHttpClient();

        Call call = client.newCall(request);

        try (Response response = call.execute()) {
            handleResponse(response);
        }

    }

    private OkHttpClient getOkHttpClient() {
        OkHttpPlugin plugin = getProject().getPlugins().findPlugin(OkHttpPlugin.class);

        OkHttpClient client;
        if (plugin != null) {
            client = plugin.getOkHttpClient();
        }
        else {
            client = new OkHttpClient();
        }
        return client;
    }

    public Request.Builder buildRequest(Request.Builder builder) {

        getHeaders().get().forEach(builder::header);

        if (getUsername().isPresent() && getPassword().isPresent()) {
            builder.header("Authorization", Credentials.basic(getUsername().get(), getPassword().get()));
        }

        if (getUrl().isPresent()) {
            builder.url(getUrl().get());
        }

        return builder;
    }

    public void handleResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            getLogger().error("{}: {}", response.code(), response.message());
            getLogger().error(response.body().string());
            throw new GradleException(response.message());
        }
    }
}
