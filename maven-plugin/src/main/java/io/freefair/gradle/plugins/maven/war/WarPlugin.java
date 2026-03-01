package io.freefair.gradle.plugins.maven.war;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Composite plugin that applies enhanced WAR functionality.
 * <p>
 * This plugin applies:
 * <ul>
 *   <li>Gradle's standard {@code war} plugin</li>
 *   <li>{@link WarOverlayPlugin} - Support for WAR overlays</li>
 *   <li>{@link WarArchiveClassesPlugin} - Archive classes separately from WAR</li>
 *   <li>{@link WarAttachClassesPlugin} - Attach classes JAR as artifact</li>
 * </ul>
 */
public class WarPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPlugins().apply(org.gradle.api.plugins.WarPlugin.class);
        project.getPlugins().apply(WarOverlayPlugin.class);
        project.getPlugins().apply(WarArchiveClassesPlugin.class);
        project.getPlugins().apply(WarAttachClassesPlugin.class);
    }
}
