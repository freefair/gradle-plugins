package io.freefair.gradle.plugins;

import lombok.Data;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

@Data
public class AspectJExtension {

    private final Property<String> version;

    @Inject
    public AspectJExtension(ObjectFactory objectFactory) {
        this.version = objectFactory.property(String.class).convention("1.9.3");
    }
}
