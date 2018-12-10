package io.freefair.gradle.plugins.builder.java;

import io.freefair.gradle.plugins.builder.io.FileBuilder;
import io.freefair.gradle.plugins.builder.io.FileWritable;
import io.freefair.gradle.plugins.builder.io.StringWritable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationBuilder implements FileWritable {
    private String name;
    private Map<String, FileWritable> properties = new HashMap<>();

    public AnnotationBuilder addProperty(String name, String value) {
        properties.put(name, new StringWritable(value));
        return this;
    }

    public AnnotationBuilder addProperty(String name, FileWritable writable) {
        properties.put(name, writable);
        return this;
    }

    public AnnotationBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public void write(FileBuilder fileBuilder) {
        fileBuilder.append("@").append(name).append("(");
        if (properties.size() > 0) {
            List<String> keys = new ArrayList<>(properties.keySet());
            for (int i = 0; i < properties.size(); i++) {
                String key = keys.get(i);
                fileBuilder.append(key);
                if (!key.isEmpty())
                    fileBuilder.append(" = ");
                properties.get(key).write(fileBuilder);
            }
        }
        fileBuilder.append(")");
    }
}
