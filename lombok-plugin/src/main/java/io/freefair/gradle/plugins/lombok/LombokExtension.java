package io.freefair.gradle.plugins.lombok;

import lombok.Data;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * @author Lars Grefer
 * @see LombokPlugin
 */
@Data
public class LombokExtension {

    /**
     * The version of Lombok which will be used.
     */
    private final Property<String> version;

    /**
     * Additional Entries for the lombok.config file.
     */
    private final MapProperty<String, String> config;

    @Inject
    public LombokExtension(ObjectFactory objectFactory) {
        version = objectFactory.property(String.class).convention("1.18.10");
        config = objectFactory.mapProperty(String.class, String.class);
    }
}
