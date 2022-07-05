package io.freefair.gradle.plugins.okhttp;

import lombok.Data;
import okhttp3.logging.HttpLoggingInterceptor;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

@Data
public class OkHttpExtension {

    private final Property<HttpLoggingInterceptor.Level> loggingLevel;

    @Inject
    public OkHttpExtension(ObjectFactory objectFactory) {
        loggingLevel = objectFactory.property(HttpLoggingInterceptor.Level.class);
    }
}
