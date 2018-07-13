package io.freefair.gradle.plugins;

import lombok.Data;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;

@Data
public class AspectJExtension {

    private final Property<String> version;

    public AspectJExtension(Project project) {
        this.version = project.getObjects().property(String.class);
    }
}
