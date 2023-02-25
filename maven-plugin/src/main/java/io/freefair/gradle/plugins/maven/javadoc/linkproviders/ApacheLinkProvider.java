package io.freefair.gradle.plugins.maven.javadoc.linkproviders;

import io.freefair.gradle.plugins.maven.javadoc.JavadocLinkProvider;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApacheLinkProvider implements JavadocLinkProvider {

    private static final Pattern tomcatVersionPattern = Pattern.compile("(\\d+\\.\\d+)\\..*");

    @Nullable
    @Override
    public String getJavadocLink(String group, String artifact, String version) {
        if (!group.startsWith("org.apache")) {
            return null;
        }

        if (group.equals("org.apache.logging.log4j") && version.startsWith("2.")) {
            if (artifact.equals("log4j-api")) {
                return "https://logging.apache.org/log4j/2.x/javadoc/log4j-api/";
            }
            else if (artifact.equals("log4j-core")) {
                return "https://logging.apache.org/log4j/2.x/javadoc/log4j-core/";
            }
        }

        if (group.startsWith("org.apache.tomcat")) {
            Matcher matcher = tomcatVersionPattern.matcher(version);
            if (matcher.matches()) {
                return "https://tomcat.apache.org/tomcat-" + matcher.group(1) + "-doc/api/";
            }
        }

        if (group.equals("org.apache.maven")) {
            return "https://maven.apache.org/ref/" + version + "/" + artifact + "/apidocs/";
        }


        return null;
    }
}
