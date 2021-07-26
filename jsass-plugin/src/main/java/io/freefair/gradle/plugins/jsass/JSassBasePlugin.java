package io.freefair.gradle.plugins.jsass;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.internal.deprecation.DeprecationLogger;

/**
 * @author Lars Grefer
 */
@Deprecated
public class JSassBasePlugin implements Plugin<Project> {

    @Getter
    private JSassExtension extension;

    @Override
    public void apply(Project project) {
        DeprecationLogger.deprecatePlugin("io.freefair.jsass-base")
                .replaceWithExternalPlugin("io.freefair.sass-base")
                .willBeRemovedInGradle8()
                .undocumented()
                .nagUser();

        this.extension = project.getExtensions().create("jsass", JSassExtension.class);

        project.getTasks().withType(SassCompile.class)
                .configureEach(compileSass -> {
                    compileSass.getIndent().convention(extension.getIndent());
                    compileSass.getLinefeed().convention(extension.getLinefeed());
                    compileSass.getOmitSourceMapUrl().convention(extension.getOmitSourceMapUrl());
                    compileSass.getOutputStyle().convention(extension.getOutputStyle());
                    compileSass.getPrecision().convention(extension.getPrecision());
                    compileSass.getSourceComments().convention(extension.getSourceComments());
                    compileSass.getSourceMapContents().convention(extension.getSourceMapContents());
                    compileSass.getSourceMapEmbed().convention(extension.getSourceMapEmbed());
                    compileSass.getSourceMapEnabled().convention(extension.getSourceMapEnabled());
                });
    }
}
