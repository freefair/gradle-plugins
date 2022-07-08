package io.freefair.gradle.plugins.github.internal;

import lombok.Data;

import java.util.Map;

@Data
public class Snapshot {

    private int version = 0;
    private String sha;
    private String ref;

    private Job job;
    private Detector detector;

    private String scanned;

    private Map<String, Manifest> manifests;

}
