package io.freefair.gradle.plugins.builder.java;

import io.freefair.gradle.plugins.builder.io.FileBuilder;
import io.freefair.gradle.plugins.builder.io.FileWritable;

import java.util.ArrayList;
import java.util.List;

public class JavaClassBuilder {

    private String packageName;
    private String className;
    private String superClass = null;
    private List<String> interfaces = new ArrayList<>();
    private FileBuilder builder;
    private List<FileWritable> members = new ArrayList<>();
    private List<AnnotationBuilder> annotations = new ArrayList<>();

    public JavaClassBuilder() {
    }

    public JavaClassBuilder(FileBuilder builder) {
        this.builder = builder;
    }

    public JavaClassBuilder setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public JavaClassBuilder setClassName(String className) {
        this.className = className;
        return this;
    }

    public JavaClassBuilder setSuperClass(String className) {
        this.superClass = className;
        return this;
    }

    public JavaClassBuilder addInterface(String interfaceName) {
        this.interfaces.add(interfaceName);
        return this;
    }

    public JavaFieldBuilder addField() {
        JavaFieldBuilder builder = new JavaFieldBuilder();
        members.add(builder);
        return builder;
    }

    public AnnotationBuilder addAnnotation() {
        AnnotationBuilder builder = new AnnotationBuilder();
        annotations.add(builder);
        return builder;
    }

    public void write() {
        if (this.builder == null) throw new IllegalArgumentException("No file builder found");
        write(this.builder);
    }

    public void write(FileBuilder builder) {
        builder.append("package ").append(packageName).append(";").appendNewLine().appendNewLine();

        if (annotations.size() > 0) {
            for (AnnotationBuilder annotation : annotations) {
                annotation.write(builder.appendNewLine());
            }
            builder.appendNewLine();
        }

        builder.append("class ").append(this.className).append(" ");
        if (superClass != null)
            builder.append("extends ").append(superClass).append(" ");
        if (interfaces.size() > 0) {
            builder.append("implements ");
            for (int i = 0; i < interfaces.size(); i++) {
                if (i > 0) builder.append(", ");
                builder.append(interfaces.get(i));
            }
            builder.append(" ");
        }

        builder.append("{").indent().appendNewLine();

        for (FileWritable writable : members) {
            writable.write(builder.appendNewLine());
        }

        builder.unindent().appendNewLine().append("}");

        builder.write();
    }
}
