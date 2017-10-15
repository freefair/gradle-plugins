package io.freefair.gradle.plugins;

import lombok.Data;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;

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
     * The {@link Configuration} which contains war files whose {@code WEB-INF/classes} directory should be added to the dependencies of the {@link #webInfClassesTarget} configuration
     *
     * @see #webInfClassesTarget
     */
    private Configuration webInfClassesSource;

    /**
     * The {@link Configuration} where the war's classes should be added to.
     *
     * @see #webInfClassesSource
     */
    private Configuration webInfClassesTarget;

    /**
     * The {@link Configuration} which contains war files whose {@code WEB-INF/lib} directory should be added to the dependencies of the {@link #webInfLibTarget} configuration
     *
     * @see #webInfLibTarget
     */
    private Configuration webInfLibSource;

    /**
     * The {@link Configuration} where the war's libs should be added to.
     *
     * @see #webInfLibSource
     */
    private Configuration webInfLibTarget;

    /**
     * Set this to false to not perform the actual overlay.
     */
    private boolean enabled = true;

    public WarOverlayExtension(Project project) {
        excludes.add("WEB-INF/lib/*.jar");
        excludes.add("META-INF/maven/**");
        excludes.add("META-INF/MANIFEST.MF");

        webInfClassesTarget = project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);
    }

    public void exclude(String pattern) {
        excludes.add(pattern);
    }

    public void exclude(String... pattern) {
        Collections.addAll(excludes, pattern);
    }
}
