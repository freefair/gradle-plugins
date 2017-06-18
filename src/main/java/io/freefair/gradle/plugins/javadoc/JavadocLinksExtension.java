package io.freefair.gradle.plugins.javadoc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.gradle.api.JavaVersion;
import org.gradle.internal.jvm.Jvm;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Lars Grefer;
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class JavadocLinksExtension {

    private JavaVersion javaVersion = Jvm.current().getJavaVersion();
    private Integer JavaEEVersion = 7;

    private List<String> links = new LinkedList<>();

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
