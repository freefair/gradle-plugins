package io.freefair.gradle.plugins.maven.version.matchers;

public interface Matcher<T> {
    boolean matches(T version);
}
