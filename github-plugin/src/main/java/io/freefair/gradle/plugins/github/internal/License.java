package io.freefair.gradle.plugins.github.internal;

import lombok.Data;

@Data
public class License {

    private String key;
    private String name;
    private String url;
    private String html_url;
    private String description;
}
