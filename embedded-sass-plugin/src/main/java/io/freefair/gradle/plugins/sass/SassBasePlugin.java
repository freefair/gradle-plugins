package io.freefair.gradle.plugins.sass;

import lombok.Getter;
import org.gradle.api.Incubating;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

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
