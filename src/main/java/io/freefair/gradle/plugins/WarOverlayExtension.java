package io.freefair.gradle.plugins;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Lars Grefer
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class WarOverlayExtension {

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Set<String> excludes = new HashSet<>();

    public WarOverlayExtension() {
        excludes.add("WEB-INF/lib/*.jar");
        excludes.add("META-INF/maven/**");
        excludes.add("META-INF/MANIFEST.MF");
    }

    public void exclude(String pattern) {
        excludes.add(pattern);
    }

    public void exclude(String... pattern) {
        Collections.addAll(excludes, pattern);
    }
}
