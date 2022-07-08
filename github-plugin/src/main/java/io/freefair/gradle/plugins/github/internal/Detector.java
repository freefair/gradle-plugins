package io.freefair.gradle.plugins.github.internal;

import lombok.Data;

@Data
public class Detector {

    private String name = "freefair-gradle";
    private String version = getClass().getPackage().getImplementationVersion();
    private String url = "https://github.com/freefair/gradle-plugins";
}
