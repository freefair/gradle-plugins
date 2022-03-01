package io.freefair.gradle.plugins.maven.javadoc.linkproviders;

import io.freefair.gradle.plugins.maven.javadoc.JavadocLinkProvider;
import org.jetbrains.annotations.Nullable;

public class SquareupLinkProvider implements JavadocLinkProvider {

    @Nullable
    @Override
    public String getJavadocLink(String group, String artifact, String version) {
        if (!group.startsWith("com.squareup")) {
            return null;
        }

        if (group.equals("com.squareup.okio")) {
            if (version.startsWith("1.")) {
                return "https://square.github.io/okio/1.x/" + artifact + "/";
            }
            else {
                return "https://square.github.io/okio/3.x/okio/okio/";
            }
        }

        if (group.equals("com.squareup.okhttp3")) {
            return "https://square.github.io/okhttp/4.x/" + artifact + "/";
        }

        if (group.equals("com.squareup.retrofit")) {
            return "https://square.github.io/retrofit/1.x/retrofit/";
        }

        if (group.equals("com.squareup.retrofit2")) {
            return "https://square.github.io/retrofit/2.x/" + artifact + "/";
        }

        return null;
    }
}
