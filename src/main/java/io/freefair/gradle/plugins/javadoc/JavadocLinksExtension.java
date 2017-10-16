package io.freefair.gradle.plugins.javadoc;

import lombok.Data;
import org.gradle.api.JavaVersion;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Lars Grefer;
 */
@Data
public class JavadocLinksExtension {

    private JavaVersion javaVersion;

    private List<String> links = new LinkedList<>();

    public JavadocLinksExtension() {
        javaVersion = JavaVersion.current();
    }

    public void links(String... links) {
        for (String link : links) {
            links(link);
        }
    }

    public void links(String link) {
        if(!links.contains(link)) {
            links.add(link);
        }
    }
}
