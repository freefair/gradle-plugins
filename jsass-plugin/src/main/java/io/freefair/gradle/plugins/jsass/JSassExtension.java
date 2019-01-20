package io.freefair.gradle.plugins.jsass;

import io.bit3.jsass.OutputStyle;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Project;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.provider.Property;

@Getter
@Setter
public class JSassExtension {

    private final Property<String> indent;

    private final Property<String> linefeed;

    private final Property<Boolean> omitSourceMapUrl;

    /**
     * Output style for the generated css code.
     */
    private final Property<OutputStyle> outputStyle;

    /**
     * Precision for outputting fractional numbers.
     */
    private final Property<Integer> precision;

    /**
     * If you want inline source comments.
     */
    private final Property<Boolean> sourceComments;

    /**
     * Embed include contents in maps.
     */
    private final Property<Boolean> sourceMapContents;

    /**
     * Embed sourceMappingUrl as data uri.
     */
    private final Property<Boolean> sourceMapEmbed;

    private final Property<Boolean> sourceMapEnabled;

    public JSassExtension(Project project) {
        indent = project.getObjects().property(String.class);
        indent.convention("  ");

        linefeed = project.getObjects().property(String.class);
        linefeed.convention(System.lineSeparator());

        omitSourceMapUrl = project.getObjects().property(Boolean.class);
        omitSourceMapUrl.convention(false);

        outputStyle = project.getObjects().property(OutputStyle.class);
        outputStyle.convention(OutputStyle.NESTED);

        precision = project.getObjects().property(Integer.class);
        precision.convention(8);

        sourceComments = project.getObjects().property(Boolean.class);
        sourceComments.convention(false);

        sourceMapContents = project.getObjects().property(Boolean.class);
        sourceMapContents.convention(false);

        sourceMapEmbed = project.getObjects().property(Boolean.class);
        sourceMapEmbed.convention(false);

        sourceMapEnabled = project.getObjects().property(Boolean.class);
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
