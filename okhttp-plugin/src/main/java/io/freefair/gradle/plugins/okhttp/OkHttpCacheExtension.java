package io.freefair.gradle.plugins.okhttp;

import lombok.Data;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

@Data
@Deprecated
public class OkHttpCacheExtension {

    private final DirectoryProperty directory;

    private final Property<Long> maxSize;

    @Inject
    public OkHttpCacheExtension(ObjectFactory objectFactory) {
        directory = objectFactory.directoryProperty();
        maxSize = objectFactory.property(Long.class);
    }
}
