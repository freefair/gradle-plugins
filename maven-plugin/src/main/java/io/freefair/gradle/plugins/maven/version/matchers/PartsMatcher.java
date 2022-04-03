package io.freefair.gradle.plugins.maven.version.matchers;

import io.freefair.gradle.plugins.maven.version.Version;
import io.freefair.gradle.plugins.maven.version.VersionPart;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class PartsMatcher implements ComparingMatcher<Version> {
    private List<ComparingMatcher<Version>> matchers;
    private ComparingMatcher<Version> qualifier;

    public PartsMatcher(List<ComparingMatcher<Version>> matchers, ComparingMatcher<Version> qualifier) {
        this.matchers = matchers;
        this.qualifier = qualifier;
    }

    @Override
    public boolean lowerThan(Version value) {
        for (int i = 0; i < matchers.size(); i++) {
            VersionPart part = value.getParts().get(i);
            ComparingMatcher<Version> matcher = matchers.get(i);
            if (matcher.greaterThan(part)) {
                return false;
            }
            if (matcher.lowerThan(part)) {
                return true;
            }
        }
        Optional<VersionPart> first = value.getParts().stream().filter(part -> Objects.equals(part.getPrefix(), "-")).findFirst();
        if(first.isPresent()) {
            int index = value.getParts().indexOf(first.get());
            List<VersionPart> collect = value.getParts().stream().skip(index).collect(Collectors.toList());
            return qualifier.lowerThan(new Version(collect));
        }
        return false;
    }

    @Override
    public boolean greaterThan(Version value) {
        for (int i = 0; i < matchers.size(); i++) {
            VersionPart part = value.getParts().get(i);
            ComparingMatcher<Version> matcher = matchers.get(i);
            if (matcher.lowerThan(part)) {
                return false;
            }
            if(matcher.greaterThan(part)) {
                return true;
            }
        }
        Optional<VersionPart> first = value.getParts().stream().filter(part -> Objects.equals(part.getPrefix(), "-")).findFirst();
        if(first.isPresent()) {
            int index = value.getParts().indexOf(first.get());
            List<VersionPart> collect = value.getParts().stream().skip(index).collect(Collectors.toList());
            return qualifier.greaterThan(new Version(collect));
        }
        return false;
    }

    @Override
    public boolean equalTo(Version value) {
        for (int i = 0; i < matchers.size(); i++) {
            VersionPart part = value.getParts().get(i);
            ComparingMatcher<Version> matcher = matchers.get(i);
            if (!matcher.equalTo(part)) {
                return false;
            }
        }
        Optional<VersionPart> first = value.getParts().stream().filter(part -> Objects.equals(part.getPrefix(), "-")).findFirst();
        if(first.isPresent() && qualifier != null) {
            int index = value.getParts().indexOf(first.get());
            List<VersionPart> collect = value.getParts().stream().skip(index).collect(Collectors.toList());
            return qualifier.equalTo(new Version(collect));
        }
        return true;
    }

    @Override
    public boolean matches(Version version) {
        return equalTo(version);
    }
}
