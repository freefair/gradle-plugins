package io.freefair.gradle.plugins.builder.gradle;

import io.freefair.gradle.plugins.builder.io.FileBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GradleConfigurationBuilder {
    private FileBuilder fileBuilder = null;
    private Set<String> plugins = new HashSet<>();
    private Map<String, List<String>> configurations = new HashMap<>();
    private List<String> customConfigurationBlocks = new ArrayList<>();
    private List<String> repositories = new ArrayList<>();

    public GradleConfigurationBuilder() {

    }

    public GradleConfigurationBuilder(FileBuilder fileBuilder) {
        this.fileBuilder = fileBuilder;
    }

    public GradleConfigurationBuilder applyPlugin(String pluginId) {
        plugins.add(pluginId);
        return this;
    }

    public GradleConfigurationBuilder addRepository(String repository) {
        this.repositories.add(repository);
        return this;
    }

    public GradleConfigurationBuilder addDependency(String configurationName, String dependency) {
        if (!configurations.containsKey(configurationName))
            configurations.put(configurationName, new ArrayList<>());
        configurations.get(configurationName).add(dependency);
        return this;
    }

    public GradleConfigurationBuilder addCustomConfigurationBlock(String block) {
        customConfigurationBlocks.add(block);
        return this;
    }

    public void write() {
        if (fileBuilder == null) throw new IllegalArgumentException("No file builder!");
        write(this.fileBuilder);
    }

    public void write(FileBuilder builder) {
        builder.append("plugins {").indent();
        for (String plugin :
                plugins) {
            builder.appendNewLine().append("id '").append(plugin).append("'");
        }
        builder.unindent().appendNewLine().append("}");
        builder.appendNewLine();

        builder.append("repositories {").indent();
        builder.appendNewLine().append("mavenCentral()").appendNewLine().append("mavenCentral()");

        for (String repository : repositories) {
            builder.appendNewLine().append("maven {")
                    .indent()
                    .appendNewLine()
                    .append("url ").append('"').append(repository).append('"')
                    .unindent().appendNewLine().append("}");
        }

        builder.unindent().appendNewLine().append("}").appendNewLine();

        builder.append("dependencies {").indent();

        for (Map.Entry<String, List<String>> stringListEntry : configurations.entrySet()) {
            for (String s : stringListEntry.getValue()) {
                builder.appendNewLine().append(stringListEntry.getKey()).append(" ");
                if (!s.contains(" ") && !s.contains(",") && !s.contains("(") && !s.contains("\""))
                    builder.append('"').append(s).append('"');
                else
                    builder.append(s);
            }
        }

        builder.unindent().appendNewLine().append("}").appendNewLine();

        for (String customConfigurationBlock : customConfigurationBlocks) {
            builder.append(customConfigurationBlock).appendNewLine();
        }

        builder.write();
    }
}
