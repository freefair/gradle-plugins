package io.freefair.gradle.plugins.maven.war;

import lombok.Data;
import org.gradle.api.provider.Property;
import org.gradle.internal.deprecation.DeprecationLogger;

/**
 * @author Lars Grefer
 */
@Data
@Deprecated
public class WarAttachClassesConvention {

    /**
     * Whether classes (that is the content of the WEB-INF/classes directory) should be attached to the project as an additional artifact.
     * <p>
     * By default the classifier for the additional artifact is 'classes'. You can change it with the {@link #classesClassifier} parameter.
     *
     * @see #classesClassifier
     * @see <a href="https://maven.apache.org/plugins/maven-war-plugin/war-mojo.html#attachClasses">maven-war-plugin#attachClasses</a>
     */
    private final Property<Boolean> attachClasses;

    /**
     * The classifier to use for the attached classes artifact.
     *
     * @see #attachClasses
     */
    private final Property<String> classesClassifier;

    public boolean isAttachClasses() {
        DeprecationLogger.deprecateProperty(WarArchiveClassesConvention.class, "attachClasses")
                .willBeRemovedInGradle8()
                .undocumented()
                .nagUser();
        return this.attachClasses.get();
    }

    public String getClassesClassifier() {
        DeprecationLogger.deprecateProperty(WarArchiveClassesConvention.class, "classesClassifier")
                .willBeRemovedInGradle8()
                .undocumented()
                .nagUser();
        return this.classesClassifier.get();
    }

    public void setAttachClasses(boolean attachClasses) {
        DeprecationLogger.deprecateProperty(WarArchiveClassesConvention.class, "attachClasses")
                .willBeRemovedInGradle8()
                .undocumented()
                .nagUser();
        this.attachClasses.convention(attachClasses);
        this.attachClasses.set(attachClasses);
    }

    public void setClassesClassifier(String classesClassifier) {
        DeprecationLogger.deprecateProperty(WarArchiveClassesConvention.class, "classesClassifier")
                .willBeRemovedInGradle8()
                .undocumented()
                .nagUser();
        this.classesClassifier.convention(classesClassifier);
        this.classesClassifier.set(classesClassifier);
    }

}
