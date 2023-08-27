package io.freefair.gradle.plugins.aspectj.internal;

import io.freefair.gradle.plugins.aspectj.WeavingSourceSet;
import lombok.Data;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.SourceSet;
import org.gradle.internal.deprecation.DeprecationLogger;

@Data
public class DefaultWeavingSourceSet implements WeavingSourceSet, HasPublicType {

    public DefaultWeavingSourceSet(SourceSet sourceSet, ObjectFactory objectFactory) {
        sourceSet.getExtensions().add("aspectPath", objectFactory.fileCollection());
        sourceSet.getExtensions().add("inPath", objectFactory.fileCollection());
    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(WeavingSourceSet.class);
    }
}
