package io.freefair.gradle.plugins.maven.version.matchers;

import io.freefair.gradle.plugins.maven.version.Version;

public class HyphenMatcher implements Matcher<Version> {
    private ComparingMatcher<Version> from;
    private ComparingMatcher<Version> to;

    public HyphenMatcher(ComparingMatcher<Version> from, ComparingMatcher<Version> to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean matches(Version versionSegment) {
        return (from.greaterThan(versionSegment) || from.equalTo(versionSegment)) && (to.lowerThan(versionSegment) || to.equalTo(versionSegment));
    }
}
