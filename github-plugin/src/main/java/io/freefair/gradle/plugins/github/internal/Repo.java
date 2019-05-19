package io.freefair.gradle.plugins.github.internal;

import lombok.Data;

@Data
public class Repo {

    private String name;
    private String html_url;
    private String description;
    private String clone_url;
    private String homepage;
    private String created_at;

    private boolean has_issues;

    private License license;
}
