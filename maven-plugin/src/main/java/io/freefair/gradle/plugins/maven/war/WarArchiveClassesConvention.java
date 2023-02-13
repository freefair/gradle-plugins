package io.freefair.gradle.plugins.maven.war;

import lombok.Data;
import org.gradle.api.provider.Property;
import org.gradle.internal.deprecation.DeprecationLogger;

@Data
@Deprecated
public class WarArchiveClassesConvention {

    /**
     * Whether a JAR file will be created for the classes in the webapp.
     * Using this optional configuration parameter will make the compiled classes to be archived into a JAR file and the classes directory will then be excluded from the webapp.
     *
     * @see <a href="https://maven.apache.org/plugins/maven-war-plugin/war-mojo.html#archiveClasses">maven-war-plugin#archiveClasses</a>
     */
    private final Property<Boolean> archiveClasses;

    public boolean isArchiveClasses() {
        DeprecationLogger.deprecateProperty(WarArchiveClassesConvention.class, "archiveClasses")
                .willBeRemovedInGradle9()
                .undocumented()
                .nagUser();
        return this.archiveClasses.get();
    }

    public void setArchiveClasses(boolean archiveClasses) {
        DeprecationLogger.deprecateProperty(WarArchiveClassesConvention.class, "archiveClasses")
                .willBeRemovedInGradle9()
                .undocumented()
                .nagUser();
        this.archiveClasses.convention(archiveClasses);
        this.archiveClasses.set(archiveClasses);
    }
}
