package io.freefair.gradle.plugins.github.internal;

import lombok.Data;

@Data
public class User {

    private String login;
    private String name;
    private String blog;
    private String html_url;
}
