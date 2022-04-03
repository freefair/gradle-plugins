package io.freefair.gradle.plugins.maven.javadoc.linkproviders;

import io.freefair.gradle.plugins.maven.javadoc.JavadocLinkProvider;
import io.freefair.gradle.plugins.maven.version.Version;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class JakartaEE9LinkProvider implements JavadocLinkProvider {

    Map<String, String> artifactVersions = new HashMap<>();

    public JakartaEE9LinkProvider() {
        artifactVersions.put("jakarta.mail", "2.");
        artifactVersions.put("jakarta.activation-api", "2.");
        artifactVersions.put("jakarta.authorization-api", "2.");
        artifactVersions.put("jakarta.batch-api", "2.");
        artifactVersions.put("jakarta.enterprise.concurrent-api", "2.");
        artifactVersions.put("jakarta.jms-api", "3.");
        artifactVersions.put("jakarta.jws-api", "3.");
        artifactVersions.put("jakarta.mail-api", "2.");
        artifactVersions.put("jakarta.jakartaee-api", "9.");
        artifactVersions.put("jakarta.jakartaee-web-api", "9.");
        artifactVersions.put("jakarta.resource-api", "2.");
        artifactVersions.put("jakarta.xml.bind-api", "3.");
        artifactVersions.put("jakarta.xml.soap-api", "2.");
        artifactVersions.put("jakarta.xml.ws-api", "3.");
        artifactVersions.put("jakarta.annotation-api", "2.");
        artifactVersions.put("jakarta.authentication-api", "2.");
        artifactVersions.put("jakarta.ejb-api", "4.");
        artifactVersions.put("jakarta.el-api", "4.");
        artifactVersions.put("jakarta.enterprise.cdi-api", "3.");
        artifactVersions.put("jakarta.faces-api", "3.");
        artifactVersions.put("jakarta.inject-api", "2.");
        artifactVersions.put("jakarta.interceptor-api", "2.");
        artifactVersions.put("jakarta.json-api", "2.");
        artifactVersions.put("jakarta.json.bind-api", "2.");
        artifactVersions.put("jakarta.persistence-api", "3.");
        artifactVersions.put("jakarta.security.enterprise-api", "2.");
        artifactVersions.put("jakarta.servlet-api", "5.");
        artifactVersions.put("jakarta.servlet.jsp-api", "3.");
        artifactVersions.put("jakarta.servlet.jsp.jstl-api", "2.");
        artifactVersions.put("jakarta.transaction-api", "2.");
        artifactVersions.put("jakarta.validation-api", "3.");
        artifactVersions.put("jakarta.websocket-api", "2.");
        artifactVersions.put("jakarta.ws.rs-api", "3.");
        artifactVersions.put("jakarta.faces", "3.");
    }

    @Override
    @Nullable
    public String getJavadocLink(String group, String artifact, Version version) {
        if (isJakarta9Dependency(group, artifact, version.toString())) {
            return "https://jakarta.ee/specifications/platform/9/apidocs/";
        }

        return null;
    }

    private boolean isJakarta9Dependency(String group, String artifact, String version) {
        if (artifactVersions.containsKey(artifact)) {
            return version.startsWith(artifactVersions.get(artifact));
        }
        return false;
    }
}
