package io.freefair.gradle.plugins.maven.javadoc.linkproviders;

import io.freefair.gradle.plugins.maven.javadoc.JavadocLinkProvider;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jetbrains.annotations.Nullable;

public class SpringLinkProvider implements JavadocLinkProvider {

    @Nullable
    @Override
    public String getJavadocLink(String group, String artifact, String version) {
        if (!group.startsWith("org.springframework")) {
            return null;
        }

        if (group.equals("org.springframework") && artifact.startsWith("spring-")) {
            return "https://docs.spring.io/spring-framework/docs/" + version + "/javadoc-api/";
        }

        if (group.equals("org.springframework.boot") && artifact.startsWith("spring-boot")) {
            String sitePrefix = "https://docs.spring.io/spring-boot/";
            String siteVersion = version;
            String sitePostfix = "/api/java/";
            ComparableVersion newDocsVersion = new ComparableVersion("3.3.0-M1");
            ComparableVersion parsedVersion = new ComparableVersion(version);

            if (parsedVersion.compareTo(newDocsVersion) < 0) {
                sitePrefix = "https://docs.spring.io/spring-boot/docs/";
                sitePostfix = "/api/";
            }
            else {
                siteVersion = version.substring(0, version.indexOf('.', version.indexOf('.') + 1));
            }

            return sitePrefix + siteVersion + sitePostfix;
        }

        if (group.equals("org.springframework.security") && artifact.startsWith("spring-security")) {
            return "https://docs.spring.io/spring-security/site/docs/" + version + "/api/";
        }

        if (group.equals("org.springframework.data") && artifact.startsWith("spring-data-")) {
            String module = artifact.replace("spring-data-", "");
            if (module.contains("-")) {
                module = module.substring(0, module.indexOf("-"));
            }
            return "https://docs.spring.io/spring-data/" + module + "/docs/" + version + "/api/";
        }

        if (group.equals("org.springframework.webflow") && artifact.equals("spring-webflow")) {
            return "https://docs.spring.io/spring-webflow/docs/" + version + "/api/";
        }

        return null;
    }
}
