package io.freefair.gradle.plugins.maven.javadoc.linkproviders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ApacheLinkProviderTest {

    private ApacheLinkProvider apacheLinkProvider;

    @BeforeEach
    void init() {
        apacheLinkProvider = new ApacheLinkProvider();
    }

    @Test
    void testTomcat10() {
        String javadocLink = apacheLinkProvider.getJavadocLink("org.apache.tomcat.embed", "tomcat-embed-core", "10.1.16");

        assertThat(javadocLink).isEqualTo("https://tomcat.apache.org/tomcat-10.1-doc/api/");
    }
}
