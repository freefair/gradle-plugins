package io.freefair.gradle.plugins.github.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class Manifest {

    /**
     * The name of the manifest.
     */
    private String name;

    private File file;

    private Map<String, Dependency> resolved = new LinkedHashMap<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class File {
        /**
         * The path of the manifest file relative to the root of the Git repository.
         */
        private String source_location;
    }

    @Data
    public static class Dependency {
        private final String package_url;

        private String relationship;

        private String scope;

        private List<String> dependencies;
    }


}
