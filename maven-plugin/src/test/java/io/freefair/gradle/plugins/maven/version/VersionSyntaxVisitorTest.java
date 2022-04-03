package io.freefair.gradle.plugins.maven.version;

import io.freefair.gradle.plugins.maven.version.matchers.Matcher;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class VersionSyntaxVisitorTest {
    @Test
    public void testSimpleVersion() {
        // Arrange
        String version = "2.3.1-SNAPSHOT";

        // Act
        Matcher<Version> versionMatcher = ParsingHelper.parseVersionSyntax(version);

        // Assert
        Version v = new Version("2.3.1-SNAPSHOT");
        assertThat(versionMatcher.matches(v)).isTrue();

        v = new Version("2.3.0-SNAPSHOT");
        assertThat(versionMatcher.matches(v)).isFalse();

        v = new Version("2.3.2-SNAPSHOT");
        assertThat(versionMatcher.matches(v)).isFalse();
    }

    @Test
    public void testLowerThan() {
        // Arrange
        String version = "<2.3.1-SNAPSHOT";

        // Act
        Matcher<Version> versionMatcher = ParsingHelper.parseVersionSyntax(version);

        // Assert
        Version v = new Version("2.3.0-SNAPSHOT");
        assertThat(versionMatcher.matches(v)).isTrue();

        v = new Version("2.3.2-SNAPSHOT");
        assertThat(versionMatcher.matches(v)).isFalse();
    }

    @Test
    public void testGreaterThan() {
        // Arrange
        String version = ">2.3.1-SNAPSHOT";

        // Act
        Matcher<Version> versionMatcher = ParsingHelper.parseVersionSyntax(version);

        // Assert
        Version v = new Version("2.3.2-SNAPSHOT");
        assertThat(versionMatcher.matches(v)).isTrue();

        v = new Version("2.3.0-SNAPSHOT");
        assertThat(versionMatcher.matches(v)).isFalse();
    }

    @Test
    public void testRange() {
        // Arrange
        String version = "2.3.1-SNAPSHOT - 2.4.0-SNAPSHOT";

        // Act
        Matcher<Version> versionMatcher = ParsingHelper.parseVersionSyntax(version);

        // Assert
        Version v = new Version("2.3.2-SNAPSHOT");
        assertThat(versionMatcher.matches(v)).isTrue();

        v = new Version("2.3.0-SNAPSHOT");
        assertThat(versionMatcher.matches(v)).isFalse();

        v = new Version("2.4.1-SNAPSHOT");
        assertThat(versionMatcher.matches(v)).isFalse();

        v = new Version("2.4.0-SNAPSHOT");
        assertThat(versionMatcher.matches(v)).isTrue();
    }

    @Test
    public void testWildcard() {
        // Arrange
        String version = "2.x";

        // Act
        Matcher<Version> versionMatcher = ParsingHelper.parseVersionSyntax(version);

        // Assert
        Version v = new Version("2.3.2-SNAPSHOT");
        assertThat(versionMatcher.matches(v)).isTrue();

        v = new Version("2.3.0-SNAPSHOT");
        assertThat(versionMatcher.matches(v)).isTrue();

        v = new Version("2.4.1-SNAPSHOT");
        assertThat(versionMatcher.matches(v)).isTrue();

        v = new Version("2.4.0-SNAPSHOT");
        assertThat(versionMatcher.matches(v)).isTrue();

        v = new Version("2.5.0");
        assertThat(versionMatcher.matches(v)).isTrue();

        v = new Version("3.0.0");
        assertThat(versionMatcher.matches(v)).isFalse();
    }
}
