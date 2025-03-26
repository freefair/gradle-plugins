package io.freefair.gradle.plugins.maven.javadoc.linkproviders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpringLinkProviderTest {

    private SpringLinkProvider linkProvider;

    @BeforeEach
    void init() {
        linkProvider = new SpringLinkProvider();
    }

    @Test
    void testSpringBoot_pre3_2_leadsToOldSite() {
        String javadocLink = linkProvider.getJavadocLink("org.springframework.boot", "spring-boot", "3.1.0-RC1");

        assertThat(javadocLink).isEqualTo("https://docs.spring.io/spring-boot/docs/3.1.0-RC1/api/");
    }

    @Test
    void testSpringBoot_3_2_leadsToOldSite() {
        String javadocLink = linkProvider.getJavadocLink("org.springframework.boot", "spring-boot", "3.2.5");

        assertThat(javadocLink).isEqualTo("https://docs.spring.io/spring-boot/docs/3.2.5/api/");
    }

    @Test
    void testSpringBoot_3_3_leadsToNewSite() {
        String javadocLink = linkProvider.getJavadocLink("org.springframework.boot", "spring-boot", "3.3.5");

        assertThat(javadocLink).isEqualTo("https://docs.spring.io/spring-boot/3.3/api/java/");
    }

    @Test
    void testSpringBoot_3_4_leadsToNewSite() {
        String javadocLink = linkProvider.getJavadocLink("org.springframework.boot", "spring-boot", "3.4.0-RC1");

        assertThat(javadocLink).isEqualTo("https://docs.spring.io/spring-boot/3.4/api/java/");
    }

}
