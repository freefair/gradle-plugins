package io.freefair.gradle.plugins.sass;

import lombok.Data;
import org.gradle.api.Incubating;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.provider.Property;
import sass.embedded_protocol.EmbeddedSass.OutputStyle;

import javax.inject.Inject;

@Data
@Incubating
public class SassExtension {

    private final Property<Boolean> omitSourceMapUrl;

    /**
     * Output style for the generated css code.
     */
    private final Property<OutputStyle> outputStyle;

    /**
     * Embed include contents in maps.
     */
    private final Property<Boolean> sourceMapContents;

    /**
     * Embed sourceMappingUrl as data uri.
     */
    private final Property<Boolean> sourceMapEmbed;

    private final Property<Boolean> sourceMapEnabled;

    @Inject
    public SassExtension(ObjectFactory objectFactory) {

        omitSourceMapUrl = objectFactory.property(Boolean.class);
        omitSourceMapUrl.convention(false);

        outputStyle = objectFactory.property(OutputStyle.class);
        outputStyle.convention(OutputStyle.EXPANDED);

        sourceMapContents = objectFactory.property(Boolean.class);
        sourceMapContents.convention(false);

        sourceMapEmbed = objectFactory.property(Boolean.class);
        sourceMapEmbed.convention(false);

        sourceMapEnabled = objectFactory.property(Boolean.class);
        sourceMapEnabled.convention(true);

        ExtraPropertiesExtension extraProperties = new DslObject(this).getExtensions().getExtraProperties();
        for (OutputStyle value : OutputStyle.values()) {
            extraProperties.set(value.name(), value);
        }
    }

    public void setOutputStyle(String outputStyle) {
        this.outputStyle.set(OutputStyle.valueOf(outputStyle.trim().toUpperCase()));
    }
}
