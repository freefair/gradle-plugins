package io.freefair.gradle.plugins.maven.javadoc.linkproviders;

import io.freefair.gradle.plugins.maven.version.Version;
import org.jetbrains.annotations.Nullable;

public class SpringLinkProvider extends AbstractLinkProvider {

    public SpringLinkProvider() {
        addArtifactLink("org.springframework", "spring-*", null, "https://docs.spring.io/spring-framework/docs/${version}/javadoc-api/");
        addArtifactLink("org.springframework.boot", "spring-boot*", null, "https://docs.spring.io/spring-boot/docs/${version}/api/");
        addArtifactLink("org.springframework.security", "spring-security*", null, "https://docs.spring.io/spring-security/site/docs/${version}/api/");
        addArtifactLink("org.springframework.webflow", "spring-webflow*", null, "https://docs.spring.io/spring-webflow/docs/${version}/api/");
    }

    @Override
    protected boolean additionalStartChecks(String group, String artifact, Version version) {
        return group.startsWith("org.springframework");
    }

    @Nullable
    @Override
    public String getJavadocLink(String group, String artifact, Version version) {
        if (group.equals("org.springframework.data") && artifact.startsWith("spring-data-")) {
            String module = artifact.replace("spring-data-", "");
            if (module.contains("-")) {
                module = module.substring(0, module.indexOf("-"));
            }
            return "https://docs.spring.io/spring-data/" + module + "/docs/" + version + "/api/";
        }

        return super.getJavadocLink(group, artifact, version);
    }
}
