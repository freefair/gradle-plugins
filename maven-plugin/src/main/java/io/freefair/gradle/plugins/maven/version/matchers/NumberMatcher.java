package io.freefair.gradle.plugins.maven.version.matchers;

import io.freefair.gradle.plugins.maven.version.Version;
import io.freefair.gradle.plugins.maven.version.VersionPart;

public class NumberMatcher implements ComparingMatcher<Version>{
    private String number;

    public NumberMatcher(String number) {
        this.number = number;
    }

    public int parseNumbers(String val) {
        String v = val.replaceAll("[^0-9]", "").trim();
        if(v.isEmpty()){
            return 0;
        }
        return Integer.parseInt(v);
    }

    @Override
    public boolean lowerThan(Version value) {
        int num = parseNumbers(number);
        if(value instanceof VersionPart)
        {
            int cmpNum = parseNumbers(((VersionPart) value).getVersionPart());
            return cmpNum < num;
        }
        return false;
    }

    @Override
    public boolean greaterThan(Version value) {
        int num = parseNumbers(number);
        if(value instanceof VersionPart)
        {
            int cmpNum = parseNumbers(((VersionPart) value).getVersionPart());
            return cmpNum > num;
        }
        return false;
    }

    @Override
    public boolean equalTo(Version value) {
        if(value instanceof VersionPart)
        {
            return number.equals(((VersionPart) value).getVersionPart());
        }
        return false;
    }

    @Override
    public boolean matches(Version version) {
        return equalTo(version);
    }
}
