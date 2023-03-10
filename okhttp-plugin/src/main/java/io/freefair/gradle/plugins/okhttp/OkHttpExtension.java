package io.freefair.gradle.plugins.okhttp;

import okhttp3.logging.HttpLoggingInterceptor;
import org.gradle.api.provider.Property;

public abstract class OkHttpExtension {

    public abstract Property<HttpLoggingInterceptor.Level> getLoggingLevel();

}
