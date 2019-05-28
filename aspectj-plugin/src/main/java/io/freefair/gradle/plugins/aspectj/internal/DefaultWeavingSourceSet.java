package io.freefair.gradle.plugins.aspectj.internal;

import io.freefair.gradle.plugins.aspectj.WeavingSourceSet;
import lombok.Data;
import org.gradle.api.file.FileCollection;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.SourceSet;

@Data
public class DefaultWeavingSourceSet implements WeavingSourceSet, HasPublicType {

    private final String aspectConfigurationName;
    private FileCollection aspectPath;

    public DefaultWeavingSourceSet(SourceSet sourceSet) {
        aspectConfigurationName = sourceSet.getTaskName("", "aspect");
    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(WeavingSourceSet.class);
    }
}
