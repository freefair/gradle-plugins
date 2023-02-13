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

    private final SourceSet sourceSet;

    public DefaultWeavingSourceSet(SourceSet sourceSet, ObjectFactory objectFactory) {
        this.sourceSet = sourceSet;

        sourceSet.getExtensions().add("aspectPath", objectFactory.fileCollection());
        sourceSet.getExtensions().add("inPath", objectFactory.fileCollection());
    }

    @Deprecated
    @Override
    public String getAspectConfigurationName() {
        DeprecationLogger.deprecateMethod(WeavingSourceSet.class, "getAspectConfigurationName()")
                .replaceWith("getAspectConfigurationName(SourceSet sourceSet)")
                .willBeRemovedInGradle9()
                .undocumented()
                .nagUser();
        return WeavingSourceSet.getAspectConfigurationName(sourceSet);
    }

    @Deprecated
    @Override
    public String getInpathConfigurationName() {
        DeprecationLogger.deprecateMethod(WeavingSourceSet.class, "getInpathConfigurationName()")
                .replaceWith("getInpathConfigurationName(SourceSet sourceSet)")
                .willBeRemovedInGradle9()
                .undocumented()
                .nagUser();
        return WeavingSourceSet.getInpathConfigurationName(sourceSet);
    }

    @Deprecated
    @Override
    public ConfigurableFileCollection getAspectPath() {
        DeprecationLogger.deprecateProperty(WeavingSourceSet.class, "aspectPath")
                .replaceWith("WeavingSourceSet.getAspectPath(SourceSet sourceSet)")
                .willBeRemovedInGradle9()
                .undocumented()
                .nagUser();
        return WeavingSourceSet.getAspectPath(sourceSet);
    }

    @Override
    @Deprecated
    public void setAspectPath(FileCollection aspectPath) {
        DeprecationLogger.deprecateProperty(WeavingSourceSet.class, "aspectPath")
                .replaceWith("WeavingSourceSet.getAspectPath(SourceSet sourceSet)")
                .willBeRemovedInGradle9()
                .undocumented()
                .nagUser();
        getAspectPath().setFrom(aspectPath);
    }

    @Deprecated
    public ConfigurableFileCollection getInPath() {
        DeprecationLogger.deprecateProperty(WeavingSourceSet.class, "inPath")
                .replaceWith("WeavingSourceSet.getInPath(SourceSet sourceSet)")
                .willBeRemovedInGradle9()
                .undocumented()
                .nagUser();
        return WeavingSourceSet.getInPath(sourceSet);
    }

    @Override
    @Deprecated
    public void setInPath(FileCollection inPath) {
        DeprecationLogger.deprecateProperty(WeavingSourceSet.class, "inPath")
                .replaceWith("WeavingSourceSet.getInPath(SourceSet sourceSet)")
                .willBeRemovedInGradle9()
                .undocumented()
                .nagUser();
        getInPath().setFrom(inPath);
    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(WeavingSourceSet.class);
    }
}
