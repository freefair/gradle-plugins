package io.freefair.gradle.plugins.maven.javadoc.linkproviders;

import io.freefair.gradle.plugins.maven.version.Version;

public class SquareupLinkProvider extends AbstractLinkProvider {

    public SquareupLinkProvider() {
        addArtifactLink("com.squareup.okio", null, "1.x", "https://square.github.io/okio/1.x/${artifact}/");
        addArtifactLink("com.squareup.okio", null, "2.x", "https://square.github.io/okio/2.x/okio/okio/");
        addArtifactLink("com.squareup.okio", null, "3.x", "https://square.github.io/okio/3.x/okio/okio/okio/");
        addArtifactLink("com.squareup.okhttp3", null, null, "https://square.github.io/okhttp/4.x/${artifact}/");
        addArtifactLink("com.squareup.retrofit", null, null, "https://square.github.io/retrofit/1.x/retrofit/");
        addArtifactLink("com.squareup.retrofit2", null, null, "https://square.github.io/retrofit/2.x/${artifact}/");
    }

    @Override
    protected boolean additionalStartChecks(String group, String artifact, Version version) {
        return group.startsWith("com.squareup");
    }
}
