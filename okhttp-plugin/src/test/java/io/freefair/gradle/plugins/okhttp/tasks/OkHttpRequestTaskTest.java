package io.freefair.gradle.plugins.okhttp.tasks;

import okhttp3.*;
import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Project;
import org.gradle.api.credentials.HttpHeaderCredentials;
import org.gradle.api.credentials.PasswordCredentials;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for OkHttpRequestTask covering credential handling,
 * HTTP responses, and error scenarios.
 */
public class OkHttpRequestTaskTest {

    private Project project;

    @BeforeEach
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void testBasicAuthCredentials() {
        OkHttpRequestTask task = project.getTasks().create("testRequest", OkHttpRequestTask.class);
        task.getUrl().set("https://example.com/test");

        PasswordCredentials credentials = project.getObjects().newInstance(PasswordCredentials.class);
        credentials.setUsername("user");
        credentials.setPassword("pass");
        task.getCredentials().set(credentials);

        Request.Builder builder = new Request.Builder();
        task.buildRequest(builder);
        Request request = builder.build();

        assertThat(request.header("Authorization")).startsWith("Basic ");
    }

    @Test
    public void testHeaderCredentials() {
        OkHttpRequestTask task = project.getTasks().create("testRequest", OkHttpRequestTask.class);
        task.getUrl().set("https://example.com/test");

        HttpHeaderCredentials credentials = project.getObjects().newInstance(HttpHeaderCredentials.class);
        credentials.setName("X-API-Key");
        credentials.setValue("secret-token");
        task.getCredentials().set(credentials);

        Request.Builder builder = new Request.Builder();
        task.buildRequest(builder);
        Request request = builder.build();

        assertThat(request.header("X-API-Key")).isEqualTo("secret-token");
    }

    @Test
    public void testUnsupportedCredentialType() {
        OkHttpRequestTask task = project.getTasks().create("testRequest", OkHttpRequestTask.class);
        task.getUrl().set("https://example.com/test");

        // Use a custom credentials implementation that's not supported
        org.gradle.api.credentials.Credentials unsupported = new org.gradle.api.credentials.Credentials() {};
        task.getCredentials().set(unsupported);

        Request.Builder builder = new Request.Builder();
        assertThatThrownBy(() -> task.buildRequest(builder))
            .isInstanceOf(InvalidUserDataException.class)
            .hasMessageContaining("Unsupported credential type");
    }

    @Test
    public void testSuccessfulResponse() throws IOException {
        OkHttpRequestTask task = project.getTasks().create("testRequest", OkHttpRequestTask.class);

        Response response = new Response.Builder()
            .request(new Request.Builder().url("https://example.com/test").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(ResponseBody.create("Success response", MediaType.get("text/plain")))
            .build();

        // Should not throw
        assertThatCode(() -> task.handleResponse(response)).doesNotThrowAnyException();
    }

    @Test
    public void test404ErrorResponse() {
        OkHttpRequestTask task = project.getTasks().create("testRequest", OkHttpRequestTask.class);

        Response response = new Response.Builder()
            .request(new Request.Builder().url("https://example.com/missing").build())
            .protocol(Protocol.HTTP_1_1)
            .code(404)
            .message("Not Found")
            .body(ResponseBody.create("Resource not found", MediaType.get("text/plain")))
            .build();

        assertThatThrownBy(() -> task.handleResponse(response))
            .isInstanceOf(GradleException.class)
            .hasMessageContaining("Not Found");
    }

    @Test
    public void test500ErrorResponse() {
        OkHttpRequestTask task = project.getTasks().create("testRequest", OkHttpRequestTask.class);

        Response response = new Response.Builder()
            .request(new Request.Builder().url("https://example.com/error").build())
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Internal Server Error")
            .body(ResponseBody.create("Server error occurred", MediaType.get("text/plain")))
            .build();

        assertThatThrownBy(() -> task.handleResponse(response))
            .isInstanceOf(GradleException.class)
            .hasMessageContaining("Internal Server Error");
    }

    @Test
    public void testErrorResponseWithEmptyBody() {
        OkHttpRequestTask task = project.getTasks().create("testRequest", OkHttpRequestTask.class);

        Response response = new Response.Builder()
            .request(new Request.Builder().url("https://example.com/error").build())
            .protocol(Protocol.HTTP_1_1)
            .code(403)
            .message("Forbidden")
            .body(ResponseBody.create("", MediaType.get("text/plain")))
            .build();

        assertThatThrownBy(() -> task.handleResponse(response))
            .isInstanceOf(GradleException.class)
            .hasMessageContaining("Forbidden");
    }

    @Test
    public void testUrlConfiguration() {
        OkHttpRequestTask task = project.getTasks().create("testRequest", OkHttpRequestTask.class);
        String testUrl = "https://example.com/api/test";
        task.getUrl().set(testUrl);

        Request.Builder builder = new Request.Builder();
        task.buildRequest(builder);
        Request request = builder.build();

        assertThat(request.url().toString()).isEqualTo(testUrl);
    }

    @Test
    public void testRequestBuilderWithoutCredentials() {
        OkHttpRequestTask task = project.getTasks().create("testRequest", OkHttpRequestTask.class);
        task.getUrl().set("https://example.com/test");

        Request.Builder builder = new Request.Builder();
        task.buildRequest(builder);
        Request request = builder.build();

        assertThat(request.header("Authorization")).isNull();
    }

    @Test
    public void testCustomHeaders() {
        OkHttpRequestTask task = project.getTasks().create("testRequest", OkHttpRequestTask.class);
        task.getUrl().set("https://example.com/test");
        task.getHeaders().put("X-Custom-Header", "custom-value");
        task.getHeaders().put("Accept", "application/json");

        Request.Builder builder = new Request.Builder();
        task.buildRequest(builder);
        Request request = builder.build();

        assertThat(request.header("X-Custom-Header")).isEqualTo("custom-value");
        assertThat(request.header("Accept")).isEqualTo("application/json");
    }
}

