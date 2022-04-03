package io.freefair.gradle.plugins.maven.javadoc.linkproviders;

import io.freefair.gradle.plugins.maven.version.Version;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ApacheLinkProviderTest {
    @Test
    public void testLog4J() {
        // Arrange
        ApacheLinkProvider linkProvider = new ApacheLinkProvider();

        // Act
        String link = linkProvider.getJavadocLink("org.apache.logging.log4j", "log4j-core", new Version("2.11.2"));

        // Assert
        assertThat(link).isEqualTo("https://logging.apache.org/log4j/2.x/log4j-core/apidocs/");
    }

    @Test
    public void testTomcat() {
        // Arrange
        ApacheLinkProvider linkProvider = new ApacheLinkProvider();

        // Act
        String link = linkProvider.getJavadocLink("org.apache.tomcat.test", "test-art", new Version("2.11.2"));

        // Assert
        assertThat(link).isEqualTo("https://tomcat.apache.org/tomcat-2.1-doc/api/");
    }

    @Test
    public void testMaven() {
        // Arrange
        ApacheLinkProvider linkProvider = new ApacheLinkProvider();

        // Act
        String link = linkProvider.getJavadocLink("org.apache.maven", "test-art", new Version("2.11.2"));

        // Assert
        assertThat(link).isEqualTo("https://maven.apache.org/ref/2.11.2/test-art/apidocs/");
    }
}
