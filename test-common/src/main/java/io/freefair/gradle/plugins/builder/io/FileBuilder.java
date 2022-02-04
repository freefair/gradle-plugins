package io.freefair.gradle.plugins.builder.io;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileBuilder {
    private File file;
    private StringBuilder stringBuilder = new StringBuilder();
    private String indentation = "";

    public FileBuilder(File file) {
        this.file = file;
    }

    public FileBuilder indent() {
        indentation += "\t";
        return this;
    }

    public FileBuilder unindent() {
        indentation = indentation.substring(0, indentation.length() - 1);
        return this;
    }

    public FileBuilder append(String str) {
        stringBuilder.append(str);
        return this;
    }

    public FileBuilder append(int i) {
        stringBuilder.append(i);
        return this;
    }

    public FileBuilder append(double d) {
        stringBuilder.append(d);
        return this;
    }

    public FileBuilder append(char c) {
        stringBuilder.append(c);
        return this;
    }

    public FileBuilder append(boolean b) {
        stringBuilder.append(b);
        return this;
    }

    public FileBuilder append(long l) {
        stringBuilder.append(l);
        return this;
    }

    public FileBuilder append(short s) {
        stringBuilder.append(s);
        return this;
    }

    public FileBuilder append(Object obj) {
        stringBuilder.append(obj);
        return this;
    }

    public FileBuilder append(float f) {
        stringBuilder.append(f);
        return this;
    }

    public FileBuilder append(char[] c) {
        stringBuilder.append(c);
        return this;
    }

    public FileBuilder append(StringBuffer sb) {
        stringBuilder.append(sb);
        return this;
    }

    public FileBuilder appendNewLine() {
        stringBuilder.append("\n").append(indentation);
        return this;
    }

    public void write() {
        try {
            Files.write(file.toPath(), stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
