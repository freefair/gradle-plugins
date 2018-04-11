package io.freefair.gradle.plugins.lombok;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class LombokExtension {

    private String version = "1.16.20";

    private Map<String, String> config = new HashMap<>();

}
