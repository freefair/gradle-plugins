package io.freefair.gradle.plugins.maven.javadoc;

import io.freefair.gradle.plugins.maven.version.Version;

import javax.annotation.Nullable;

public interface JavadocLinkProvider {

    @Nullable
    String getJavadocLink(String group, String artifact, Version version);
}
