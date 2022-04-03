package io.freefair.gradle.plugins.maven.version.matchers;

import io.freefair.gradle.plugins.maven.version.Version;

public class AllMatcher implements ComparingMatcher<Version> {
    @Override
    public boolean matches(Version versionSegment) {
        return true;
    }

    @Override
    public boolean lowerThan(Version value) {
        return true;
    }

    @Override
    public boolean greaterThan(Version value) {
        return true;
    }

    @Override
    public boolean equalTo(Version value) {
        return true;
    }
}
