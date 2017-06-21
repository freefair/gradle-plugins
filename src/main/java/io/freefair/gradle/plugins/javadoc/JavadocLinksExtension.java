package io.freefair.gradle.plugins.javadoc;

import lombok.Data;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.provider.PropertyState;
import org.gradle.api.provider.Provider;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Lars Grefer;
 */
@Data
public class JavadocLinksExtension {

    private final PropertyState<JavaVersion> javaVersion;

    private List<String> links = new LinkedList<>();

    public JavadocLinksExtension(Project project) {
        javaVersion = project.property(JavaVersion.class);
        javaVersion.set(project.provider(new Callable<JavaVersion>() {
            @Override
            public JavaVersion call() throws Exception {
                return JavaVersion.current();
            }
        }));
    }

    public void setJavaVersion(JavaVersion javaVersion) {
        this.javaVersion.set(javaVersion);
    }

    public Provider<JavaVersion> getJavaVersionProvider() {
        return javaVersion;
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
