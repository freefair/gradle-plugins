package io.freefair.gradle.plugins.jsass;

import io.bit3.jsass.OutputStyle;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Project;
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
        indent.set("  ");

        linefeed = project.getObjects().property(String.class);
        linefeed.set(System.lineSeparator());

        omitSourceMapUrl = project.getObjects().property(Boolean.class);
        omitSourceMapUrl.set(false);

        outputStyle = project.getObjects().property(OutputStyle.class);
        outputStyle.set(OutputStyle.NESTED);

        precision = project.getObjects().property(Integer.class);
        precision.set(8);

        sourceComments = project.getObjects().property(Boolean.class);
        sourceComments.set(false);

        sourceMapContents = project.getObjects().property(Boolean.class);
        sourceMapContents.set(false);

        sourceMapEmbed = project.getObjects().property(Boolean.class);
        sourceMapEmbed.set(false);

        sourceMapEnabled = project.getObjects().property(Boolean.class);
        sourceMapEnabled.set(true);
    }
}
