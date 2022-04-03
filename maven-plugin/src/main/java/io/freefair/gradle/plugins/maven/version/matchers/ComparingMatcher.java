package io.freefair.gradle.plugins.maven.version.matchers;

public interface ComparingMatcher<T> extends Matcher<T> {
    boolean lowerThan(T value);
    boolean greaterThan(T value);
    boolean equalTo(T value);
}
