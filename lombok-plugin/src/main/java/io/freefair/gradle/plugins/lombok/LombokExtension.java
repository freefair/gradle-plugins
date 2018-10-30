package io.freefair.gradle.plugins.lombok;

import lombok.Data;

import java.util.Map;
import java.util.TreeMap;

@Data
public class LombokExtension {

    private String version = "1.18.4";

    private Map<String, String> config = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

}
