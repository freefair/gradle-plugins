package io.freefair.gradle.plugins.sass;

import lombok.Getter;
import org.gradle.api.Incubating;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Base plugin for compiling Sass/SCSS files using the embedded Dart Sass compiler.
 * <p>
 * Creates a {@code sass} extension for configuring default compilation settings that
 * apply to all {@link SassCompile} tasks, including:
 * <ul>
 *   <li>Output style (compressed, expanded, etc.)</li>
 *   <li>Source map generation and embedding</li>
 *   <li>Source map URL inclusion</li>
 * </ul>
 *
 * @see SassExtension
 * @see SassCompile
 */
@Incubating
public class SassBasePlugin implements Plugin<Project> {
    @Getter
    private SassExtension extension;

    @Override
    public void apply(Project project) {
        this.extension = project.getExtensions().create("sass", SassExtension.class);

        project.getTasks().withType(SassCompile.class)
                .configureEach(compileSass -> {
                    compileSass.getOmitSourceMapUrl().convention(extension.getOmitSourceMapUrl());
                    compileSass.getOutputStyle().convention(extension.getOutputStyle());
                    compileSass.getSourceMapContents().convention(extension.getSourceMapContents());
                    compileSass.getSourceMapEmbed().convention(extension.getSourceMapEmbed());
                    compileSass.getSourceMapEnabled().convention(extension.getSourceMapEnabled());
                });
    }
}
