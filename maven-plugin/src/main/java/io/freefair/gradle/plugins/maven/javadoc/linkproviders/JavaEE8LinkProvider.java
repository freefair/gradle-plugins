package io.freefair.gradle.plugins.maven.javadoc.linkproviders;

import io.freefair.gradle.plugins.maven.javadoc.JavadocLinkProvider;
import io.freefair.gradle.plugins.maven.version.Version;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class JavaEE8LinkProvider implements JavadocLinkProvider {

    Map<String, String> artifactVersions = new HashMap<>();

    public JavaEE8LinkProvider() {
        artifactVersions.put("javax.mail", "1,");
        artifactVersions.put("javaee-api", "8.");
        artifactVersions.put("javaee-web-api", "8.");
        artifactVersions.put("javax.batch-api", "1.");
        artifactVersions.put("javax.enterprise.concurrent-api", "1.");
        artifactVersions.put("javax.enterprise.deploy-api", "1.");
        artifactVersions.put("javax.jms-api", "2.");
        artifactVersions.put("javax.management.j2ee-api", "1.");
        artifactVersions.put("javax.resource-api", "1.");
        artifactVersions.put("javax.security.jacc-api", "1.");
        artifactVersions.put("javax.xml.registry-api", "1.");
        artifactVersions.put("javax.xml.rpc-api", "1.");
        artifactVersions.put("javax.annotation-api", "1.");
        artifactVersions.put("javax.ejb-api", "3.");
        artifactVersions.put("javax.el-api", "3.");
        artifactVersions.put("cdi-api", "2.");
        artifactVersions.put("javax.faces-api", "2.");
        artifactVersions.put("javax.inject", "1");
        artifactVersions.put("javax.interceptor-api", "1.");
        artifactVersions.put("javax.json-api", "1.");
        artifactVersions.put("javax.json.bind-api", "1.");
        artifactVersions.put("javax.security.auth.message-api", "1.");
        artifactVersions.put("javax.security.enterprise-api", "1.");
        artifactVersions.put("javax.servlet-api", "4.");
        artifactVersions.put("javax.servlet.jsp-api", "2.");
        artifactVersions.put("javax.servlet.jsp.jstl-api", "1.");
        artifactVersions.put("javax.transaction-api", "1.");
        artifactVersions.put("validation-api", "2.");
        artifactVersions.put("javax.websocket-api", "1.");
        artifactVersions.put("javax.ws.rs-api", "2.");
        artifactVersions.put("javax.persistence", "2.");
        artifactVersions.put("javax.faces", "2.");
    }

    @Override
    @Nullable
    public String getJavadocLink(String group, String artifact, Version version) {
        if (isJavaEE8Dependency(group, artifact, version.toString())) {
            return "https://javaee.github.io/javaee-spec/javadocs/";
        }

        return null;
    }

    private boolean isJavaEE8Dependency(String group, String artifact, String version) {
        if (artifactVersions.containsKey(artifact)) {
            return version.startsWith(artifactVersions.get(artifact));
        }
        return false;
    }
}
