package io.freefair.gradle.plugins.gwt;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

/**
 * @author Lars Grefer
 */
public abstract class GwtExtension {

    public abstract Property<String> getToolVersion();

    public abstract ListProperty<String> getModules();

    public GwtExtension() {
        getToolVersion().convention("2.13.0");
    }
}
