package io.freefair.gradle.plugins.sass;

import com.sass_lang.embedded_protocol.OutputStyle;
import org.gradle.api.Incubating;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.provider.Property;

@Incubating
public abstract class SassExtension implements ExtensionAware {

    public abstract Property<Boolean> getOmitSourceMapUrl();

    /**
     * Output style for the generated css code.
     */
    public abstract Property<OutputStyle> getOutputStyle();

    /**
     * Embed include contents in maps.
     */
    public abstract Property<Boolean> getSourceMapContents();

    /**
     * Embed sourceMappingUrl as data uri.
     */
    public abstract Property<Boolean> getSourceMapEmbed();

    public abstract Property<Boolean> getSourceMapEnabled();

    public SassExtension() {
        getOmitSourceMapUrl().convention(false);
        getOutputStyle().convention(OutputStyle.EXPANDED);
        getSourceMapContents().convention(false);
        getSourceMapEmbed().convention(false);
        getSourceMapEnabled().convention(true);

        ExtraPropertiesExtension extraProperties = this.getExtensions().getExtraProperties();
        for (OutputStyle value : OutputStyle.values()) {
            extraProperties.set(value.name(), value);
        }
    }

}
