package io.freefair.gradle.plugins.lombok;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.internal.deprecation.DeprecationLogger;

import javax.inject.Inject;

/**
 * @author Lars Grefer
 * @see LombokPlugin
 */
@Getter
@Setter
public class LombokExtension {

    public static final String LOMBOK_VERSION = "1.18.22";

    /**
     * The version of Lombok which will be used.
     */
    private final Property<String> version;

    /**
     * Additional Entries for the lombok.config file.
     */
    @Deprecated
    private final MapProperty<String, String> config;

    private final Property<Boolean> disableConfig;

    @Inject
    public LombokExtension(ObjectFactory objectFactory) {
        version = objectFactory.property(String.class).convention(LOMBOK_VERSION);
        config = objectFactory.mapProperty(String.class, String.class);
        disableConfig = objectFactory.property(Boolean.class);

        disableConfig.convention(System.getProperty("lombok.disableConfig") != null);
    }

    @Deprecated
    public MapProperty<String, String> getConfig() {
        DeprecationLogger.deprecateProperty(LombokExtension.class, "config")
                .willBeRemovedInGradle8()
                .undocumented()
                .nagUser();
        return config;
    }
}
