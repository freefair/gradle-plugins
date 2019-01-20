package io.freefair.gradle.plugins.jsass;

import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Lars Grefer
 */
public class JSassBasePlugin implements Plugin<Project> {

    @Getter
    private JSassExtension extension;

    @Override
    public void apply(Project project) {
        this.extension = project.getExtensions().create("jsass", JSassExtension.class, project);

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
