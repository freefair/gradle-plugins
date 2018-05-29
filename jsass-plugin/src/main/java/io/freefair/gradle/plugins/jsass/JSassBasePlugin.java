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

        project.getTasks().withType(SassCompile.class, compileSass -> {
            compileSass.getIndent().set(extension.getIndent());
            compileSass.getLinefeed().set(extension.getLinefeed());
            compileSass.getOmitSourceMapUrl().set(extension.getOmitSourceMapUrl());
            compileSass.getOutputStyle().set(extension.getOutputStyle());
            compileSass.getPrecision().set(extension.getPrecision());
            compileSass.getSourceComments().set(extension.getSourceComments());
            compileSass.getSourceMapContents().set(extension.getSourceMapContents());
            compileSass.getSourceMapEmbed().set(extension.getSourceMapEmbed());
            compileSass.getSourceMapEnabled().set(extension.getSourceMapEnabled());
        });
    }
}
