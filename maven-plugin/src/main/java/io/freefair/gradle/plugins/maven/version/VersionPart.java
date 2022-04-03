package io.freefair.gradle.plugins.maven.version;

import lombok.Getter;
import org.gradle.internal.impldep.org.testng.collections.Lists;

@Getter
public class VersionPart extends Version {
    @Getter
    private final String versionPart;
    @Getter
    private final String prefix;

    public VersionPart(String prefix, String versionPart) {
        super(Lists.newArrayList());
        this.versionPart = versionPart;
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return prefix + versionPart;
    }
}
