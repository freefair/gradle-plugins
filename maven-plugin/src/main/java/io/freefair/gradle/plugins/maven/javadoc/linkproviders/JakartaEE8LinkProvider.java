package io.freefair.gradle.plugins.maven.javadoc.linkproviders;

import io.freefair.gradle.plugins.maven.javadoc.JavadocLinkProvider;
import io.freefair.gradle.plugins.maven.version.Version;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class JakartaEE8LinkProvider implements JavadocLinkProvider {

    Map<String, String> artifactVersions = new HashMap<>();

    public JakartaEE8LinkProvider() {
        artifactVersions.put("jakarta.annotation-api", "1.");
        artifactVersions.put("jakarta.ejb-api", "3.");
        artifactVersions.put("jakarta.el-api", "3.");
        artifactVersions.put("jakarta.enterprise.cdi-api", "2.");
        artifactVersions.put("jakarta.faces-api", "2.");
        artifactVersions.put("jakarta.inject-api", "1.");
        artifactVersions.put("jakarta.interceptor-api", "1.");
        artifactVersions.put("jakarta.json-api", "1.");
        artifactVersions.put("jakarta.json.bind-api", "1.");
        artifactVersions.put("jakarta.persistence-api", "2.");
        artifactVersions.put("jakarta.security.auth.message-api", "1.");
        artifactVersions.put("jakarta.security.enterprise-api", "1.");
        artifactVersions.put("jakarta.servlet-api", "4.");
        artifactVersions.put("jakarta.servlet.jsp-api", "2.");
        artifactVersions.put("jakarta.servlet.jsp.jstl-api", "1.");
        artifactVersions.put("jakarta.transaction-api", "1.");
        artifactVersions.put("jakarta.validation-api", "2.");
        artifactVersions.put("jakarta.websocket-api", "1.");
        artifactVersions.put("jakarta.ws.rs-api", "2.");
        artifactVersions.put("jakarta.mail", "1.");
        artifactVersions.put("jakarta.authorization-api", "1.");
        artifactVersions.put("jakarta.batch-api", "1.");
        artifactVersions.put("jakarta.enterprise.concurrent-api", "1.");
        artifactVersions.put("jakarta.enterprise.deploy-api", "1.");
        artifactVersions.put("jakarta.jms-api", "2.");
        artifactVersions.put("jakarta.mail-api", "1.");
        artifactVersions.put("jakarta.management.j2ee-api", "1.");
        artifactVersions.put("jakarta.jakartaee-web-api", "8.");
        artifactVersions.put("jakarta.jakartaee-api", "8.");
        artifactVersions.put("jakarta.resource-api", "1.");
        artifactVersions.put("jakarta.xml.registry-api", "1.");
        artifactVersions.put("jakarta.xml.rpc-api", "1.");
        artifactVersions.put("jakarta.faces", "2.");
    }

    @Override
    @Nullable
    public String getJavadocLink(String group, String artifact, Version version) {
        if (isJakarta8Dependency(group, artifact, version)) {
            return "https://jakarta.ee/specifications/platform/8/apidocs/";
        }

        return null;
    }

    private boolean isJakarta8Dependency(String group, String artifact, Version version) {
        if (artifactVersions.containsKey(artifact)) {
            return version.toString().startsWith(artifactVersions.get(artifact));
        }
        return false;
    }
}
