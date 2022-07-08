package io.freefair.gradle.plugins.github.internal;

import lombok.Data;

@Data
public class Job {

    private String id;
    private String correlator;
    private String html_url;

}
