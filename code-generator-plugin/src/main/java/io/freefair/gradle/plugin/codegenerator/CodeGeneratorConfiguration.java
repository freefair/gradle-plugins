package io.freefair.gradle.plugin.codegenerator;

import lombok.AccessLevel;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter(AccessLevel.PACKAGE)
public class CodeGeneratorConfiguration {
    private List<File> generatorJars = new ArrayList<>();
    private Map<String, Object> configurationValues = new HashMap<>();

    /**
     * Add files to generator jars
     * @param file File to add to generator jars
     */
    public void generatorJar(File file) {
        generatorJars.add(file);
    }

    /**
     * Add file to generator jars
     * @param filename Filename to add to generator jars
     */
    public void generatorJar(String filename) {
        generatorJars.add(new File(filename));
    }

    public void param(String key, Object value) {
        configurationValues.put(key, value);
    }
}
