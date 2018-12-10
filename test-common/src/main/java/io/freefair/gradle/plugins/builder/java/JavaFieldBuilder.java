package io.freefair.gradle.plugins.builder.java;

import io.freefair.gradle.plugins.builder.io.FileBuilder;
import io.freefair.gradle.plugins.builder.io.FileWritable;

import java.util.ArrayList;
import java.util.List;

public class JavaFieldBuilder implements FileWritable {

  private String name;
  private String type;
  private String accessor = "private";
  private String defaultValue = null;

  private boolean isFinal = false;
  private List<AnnotationBuilder> annotations = new ArrayList<>();

  public JavaFieldBuilder() {
  }

  public JavaFieldBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public JavaFieldBuilder setType(String type) {
    this.type = type;
    return this;
  }

  public JavaFieldBuilder setAccessor(String accessor) {
    this.accessor = accessor;
    return this;
  }

  public JavaFieldBuilder setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  public JavaFieldBuilder isFinal() {
    this.isFinal = true;
    return this;
  }

  public AnnotationBuilder addAnnotation() {
    AnnotationBuilder annotation = new AnnotationBuilder();
    this.annotations.add(annotation);
    return annotation;
  }

  @Override
  public void write(FileBuilder builder) {
    if (annotations.size() > 0) {
      for (AnnotationBuilder annotation : annotations) {
        annotation.write(builder.appendNewLine());
      }
      builder.appendNewLine();
    }
    builder.append(accessor).append(" ");
    if (isFinal)
      builder.append("final ");
    builder.append(type).append(" ").append(name);
    if (defaultValue != null)
      builder.append(" = ").append(defaultValue);
    builder.append(";");
  }
}
