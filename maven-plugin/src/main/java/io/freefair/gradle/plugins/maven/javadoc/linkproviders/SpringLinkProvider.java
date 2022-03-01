package io.freefair.gradle.plugins.maven.javadoc.linkproviders;

import io.freefair.gradle.plugins.maven.javadoc.JavadocLinkProvider;
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
            return "https://docs.spring.io/spring-boot/docs/" + version + "/api/";
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
