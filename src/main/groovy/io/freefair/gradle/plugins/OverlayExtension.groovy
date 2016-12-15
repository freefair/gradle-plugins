package io.freefair.gradle.plugins

/**
 * @author Lars Grefer
 */
class OverlayExtension {

    List<String> excludes = ["WEB-INF/lib/*.jar"];

    void exclude(String pattern) {
        excludes.add(pattern);
    }
}
