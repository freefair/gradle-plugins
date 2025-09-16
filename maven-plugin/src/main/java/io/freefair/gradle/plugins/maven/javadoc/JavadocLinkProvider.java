package io.freefair.gradle.plugins.maven.javadoc;

import kotlin.jvm.functions.Function3;

import javax.annotation.Nullable;

public interface JavadocLinkProvider extends Function3<String, String, String, String> {

    @Nullable
    String getJavadocLink(String group, String artifact, String version);

    @Override
    default String invoke(String group, String artifact, String version) {
        return this.getJavadocLink(group, artifact, version);
    }
}
