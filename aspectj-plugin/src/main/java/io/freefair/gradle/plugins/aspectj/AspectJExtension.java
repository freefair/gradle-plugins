package io.freefair.gradle.plugins.aspectj;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

@Getter
@Setter
public class AspectJExtension {

    private final Property<String> version;

    @Inject
    public AspectJExtension(ObjectFactory objectFactory) {
        this.version = objectFactory.property(String.class).convention("1.9.7");
    }
}
