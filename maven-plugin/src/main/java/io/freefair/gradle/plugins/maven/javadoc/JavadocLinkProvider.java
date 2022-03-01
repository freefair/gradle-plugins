package io.freefair.gradle.plugins.maven.javadoc;

import javax.annotation.Nullable;

public interface JavadocLinkProvider {

    @Nullable
    String getJavadocLink(String group, String artifact, String version);
}
