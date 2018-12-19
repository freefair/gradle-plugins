package io.freefair.gradle.plugin.codegenerator;

import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;

import javax.inject.Inject;

@Getter(AccessLevel.PACKAGE)
public class CodeGeneratorConfiguration {

    private final MapProperty<String, Object> configurationValues;

    @Inject
    public CodeGeneratorConfiguration(ObjectFactory objectFactory) {
        this.configurationValues = objectFactory.mapProperty(String.class, Object.class);
    }

    public void param(String key, Object value) {
        configurationValues.put(key, value);
    }
}
