package io.freefair.gradle.plugins.maven.version.matchers;

import io.freefair.gradle.plugins.maven.version.Version;
import io.freefair.gradle.plugins.maven.version.VersionPart;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class QualifierMatcher implements ComparingMatcher<Version> {
    private List<ComparingMatcher<Version>> parts;

    public QualifierMatcher(List<ComparingMatcher<Version>> parts) {
        this.parts = parts;
    }

    @Override
    public boolean lowerThan(Version value) {
        for (int i = 0; i < parts.size(); i++) {
            VersionPart part = value.getParts().get(i);
            ComparingMatcher<Version> matcher = parts.get(i);
            if (matcher.greaterThan(part)) {
                return false;
            }
            if(matcher.lowerThan(part)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean greaterThan(Version value) {
        for (int i = 0; i < parts.size(); i++) {
            VersionPart part = value.getParts().get(i);
            ComparingMatcher<Version> matcher = parts.get(i);
            if (!matcher.lowerThan(part)) {
                return false;
            }
            if(matcher.greaterThan(part)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equalTo(Version value) {
        for (int i = 0; i < parts.size(); i++) {
            VersionPart part = value.getParts().get(i);
            ComparingMatcher<Version> matcher = parts.get(i);
            if (!matcher.equalTo(part)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean matches(Version version) {
        return equalTo(version);
    }
}
