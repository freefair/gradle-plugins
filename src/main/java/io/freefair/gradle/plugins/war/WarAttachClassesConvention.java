package io.freefair.gradle.plugins.war;

import lombok.Data;

/**
 * @author Lars Grefer
 */
@Data
public class WarAttachClassesConvention {

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

}
