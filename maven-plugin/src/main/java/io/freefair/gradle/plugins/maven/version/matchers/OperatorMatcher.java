package io.freefair.gradle.plugins.maven.version.matchers;

import io.freefair.gradle.plugins.maven.version.Version;

public class OperatorMatcher implements Matcher<Version> {
    private String operator;
    private ComparingMatcher<Version> partsMatcher;

    public OperatorMatcher(String operator, ComparingMatcher<Version> partsMatcher) {
        this.operator = operator;
        this.partsMatcher = partsMatcher;
    }

    @Override
    public boolean matches(Version version) {
        if(operator == null || operator.equals("=")) {
            return partsMatcher.matches(version);
        } else if(operator.equals("<")) {
            return partsMatcher.lowerThan(version);
        } else if (operator.equals(">")) {
            return partsMatcher.greaterThan(version);
        } else if (operator.equals("<=")) {
            return partsMatcher.lowerThan(version) || partsMatcher.equalTo(version);
        } else if (operator.equals(">=")) {
            return partsMatcher.greaterThan(version) || partsMatcher.equalTo(version);
        } else if (operator.equals("!=")) {
            return !partsMatcher.matches(version);
        }
        return false;
    }
}
