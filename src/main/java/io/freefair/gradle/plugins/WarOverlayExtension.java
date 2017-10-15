package io.freefair.gradle.plugins;

import lombok.Data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Lars Grefer
 */
@Data
public class WarOverlayExtension {

    private Set<String> excludes = new HashSet<>();

    /**
     * Whether classes (that is the content of the WEB-INF/classes directory) should be attached to the project as an additional artifact.
     * <p>
     * By default the classifier for the additional artifact is 'classes'. You can change it with the {@link #classesClassifier} parameter.
     *
     * @see #classesClassifier
     * @see <a href="https://maven.apache.org/plugins/maven-war-plugin/war-mojo.html#attachClasses">maven-war-plugin#attachClasses</a>
     */
    private boolean attachClasses = false;

    /**
     * The classifier to use for the attached classes artifact.
     *
     * @see #attachClasses
     */
    private String classesClassifier = "classes";

    /**
     * Allow compilation against the classes contained in war dependencies
     */
    private boolean attachWebInfClasses = true;

    /**
     * Allow compilation against the libs contained in war dependencies
     */
    private boolean attachWebInfLib = false;

    public WarOverlayExtension() {
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
