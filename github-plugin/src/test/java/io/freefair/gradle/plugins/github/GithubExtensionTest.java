package io.freefair.gradle.plugins.github;

import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GithubExtensionTest {

    private GithubExtension githubExtension;

    @BeforeEach
    void setUp() {
        githubExtension = new GithubExtension(
                ProjectBuilder.builder().build().getObjects()
        );
    }

    @Test
    void testSetSlug() {
        githubExtension.getSlug().set("foo/bar");

        assertThat(githubExtension.getOwner().get()).isEqualTo("foo");
        assertThat(githubExtension.getRepo().get()).isEqualTo("bar");
    }

    @Test
    void testSetOwnerAndRepo() {
        githubExtension.setOwner("foo");
        githubExtension.setRepo("bar");

        assertThat(githubExtension.getSlug().get()).isEqualTo("foo/bar");
    }
}
