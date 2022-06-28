package io.freefair.gradle.plugins.maven.javadoc.linkproviders;

import io.freefair.gradle.plugins.maven.javadoc.JavadocLinkProvider;
import org.jetbrains.annotations.Nullable;

public class ApacheLinkProvider implements JavadocLinkProvider {

    @Nullable
    @Override
    public String getJavadocLink(String group, String artifact, String version) {
        if (!group.startsWith("org.apache")) {
            return null;
        }

        if (group.equals("org.apache.logging.log4j") && version.startsWith("2.")) {
            return "https://logging.apache.org/log4j/2.x/" + artifact + "/apidocs/";
        }

        if (group.startsWith("org.apache.tomcat")) {
            return "https://tomcat.apache.org/tomcat-" + version.substring(0, 3) + "-doc/api/";
        }

        if (group.equals("org.apache.maven")) {
            return "https://maven.apache.org/ref/" + version + "/" + artifact + "/apidocs/";
        }


        return null;
    }
}
