package io.freefair.gradle.plugins.github.internal;

import lombok.Data;

@Data
public class UploadSnapshotResponse {

    private long id;
    private String created_at;
    private String message;
    private String result;
}
