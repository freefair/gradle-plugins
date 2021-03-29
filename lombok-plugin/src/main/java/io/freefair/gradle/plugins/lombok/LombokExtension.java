package io.freefair.gradle.plugins.lombok;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * @author Lars Grefer
 * @see LombokPlugin
 */
@Slf4j
@Getter
@Setter
public class LombokExtension {

    /**
     * The version of Lombok which will be used.
     */
    private final Property<String> version;

    /**
     * Additional Entries for the lombok.config file.
     */
    @Deprecated
    private final MapProperty<String, String> config;

    @Inject
    public LombokExtension(ObjectFactory objectFactory) {
        version = objectFactory.property(String.class).convention("1.18.18");
        config = objectFactory.mapProperty(String.class, String.class);
    }

    @Deprecated
    public MapProperty<String, String> getConfig() {
        log.warn("'lombok.config' is deprecated", new Exception("stacktrace"));
        return config;
    }
}
