package io.freefair.gradle.plugins.okhttp.tasks;

import io.freefair.gradle.plugins.okhttp.OkHttpPlugin;
import lombok.Getter;
import lombok.Setter;
import okhttp3.*;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

/**
 * @author Lars Grefer
 */
@Getter
@Setter
public abstract class OkHttpRequestTask extends DefaultTask {

    @Input
    private final Property<String> username = getProject().getObjects().property(String.class);

    @Input
    private final Property<String> password = getProject().getObjects().property(String.class);

    @Input
    private final Property<String> url = getProject().getObjects().property(String.class);

    @Input
    private final MapProperty<String, String> headers = getProject().getObjects().mapProperty(String.class, String.class);

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

        headers.get().forEach(builder::header);

        if (username.isPresent() && password.isPresent()) {
            builder.header("Authorization", Credentials.basic(this.username.get(), this.password.get()));
        }

        if (url.isPresent()) {
            builder.url(url.get());
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
