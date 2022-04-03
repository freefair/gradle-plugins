package io.freefair.gradle.plugins.maven.javadoc.linkproviders;

import io.freefair.gradle.plugins.maven.version.Version;

public class ApacheLinkProvider extends AbstractLinkProvider {
    public ApacheLinkProvider() {
        addArtifactLink("org.apache.logging.log4j", null, "2.x", "https://logging.apache.org/log4j/2.x/${artifact}/apidocs/");
        addArtifactLink("org.apache.tomcat*", null, null, "https://tomcat.apache.org/tomcat-${version:0,3}-doc/api/");
        addArtifactLink("org.apache.maven", null, null, "https://maven.apache.org/ref/${version}/${artifact}/apidocs/");
    }

    @Override
    protected boolean additionalStartChecks(String group, String artifact, Version version) {
        return group.startsWith("org.apache");
    }
}
