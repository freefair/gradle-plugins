package io.freefair.gradle.plugins.okhttp.tasks;

import okhttp3.Request;
import org.gradle.work.DisableCachingByDefault;

/**
 * @author Lars Grefer
 */
@DisableCachingByDefault(because = "Remote state cannot be tracked")
public abstract class HttpGet extends OkHttpRequestTask {

    @Override
    public Request.Builder buildRequest(Request.Builder builder) {
        return super.buildRequest(builder)
                .get();
    }
}
