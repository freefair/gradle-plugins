package io.freefair.gradle.plugins.lombok;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * @author Lars Grefer
 * @see LombokPlugin
 */
@Getter
@Setter
public class LombokExtension {

    public static final String LOMBOK_VERSION = "1.18.24";

    /**
     * The version of Lombok which will be used.
     */
    private final Property<String> version;

    private final Property<Boolean> disableConfig;

    @Inject
    public LombokExtension(ObjectFactory objectFactory) {
        version = objectFactory.property(String.class).convention(LOMBOK_VERSION);
        disableConfig = objectFactory.property(Boolean.class);

        disableConfig.convention(System.getProperty("lombok.disableConfig") != null);
    }
}
