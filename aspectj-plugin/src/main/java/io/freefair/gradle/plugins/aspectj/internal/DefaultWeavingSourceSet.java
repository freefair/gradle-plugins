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

    private final String inpathConfigurationName;
    private FileCollection inPath;

    public DefaultWeavingSourceSet(SourceSet sourceSet) {
        aspectConfigurationName = sourceSet.getTaskName("", "aspect");
        inpathConfigurationName = sourceSet.getTaskName("", "inpath");
    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(WeavingSourceSet.class);
    }
}
