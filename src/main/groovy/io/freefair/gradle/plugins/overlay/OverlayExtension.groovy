package io.freefair.gradle.plugins.overlay

/**
 * @author Lars Grefer
 */
class OverlayExtension {

    List<String> excludes = ["WEB-INF/lib/*.jar"];

    void exclude(String pattern) {
        excludes.add(pattern);
    }
}
