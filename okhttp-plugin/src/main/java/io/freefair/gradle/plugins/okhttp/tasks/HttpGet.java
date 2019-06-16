package io.freefair.gradle.plugins.okhttp.tasks;

import okhttp3.Request;

/**
 * @author Lars Grefer
 */
public class HttpGet extends OkHttpRequestTask {

    @Override
    public Request.Builder buildRequest(Request.Builder builder) {
        return super.buildRequest(builder)
                .get();
    }
}
