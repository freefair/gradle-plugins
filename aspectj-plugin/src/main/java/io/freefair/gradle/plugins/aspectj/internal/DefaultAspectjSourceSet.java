package io.freefair.gradle.plugins.aspectj.internal;

import groovy.lang.Closure;
import io.freefair.gradle.plugins.aspectj.AspectjSourceDirectorySet;
import io.freefair.gradle.plugins.aspectj.AspectjSourceSet;
import io.freefair.gradle.plugins.aspectj.WeavingSourceSet;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.tasks.DefaultSourceSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.SourceSet;
import org.gradle.internal.deprecation.DeprecationLogger;
import org.gradle.util.ConfigureUtil;

import javax.annotation.Nullable;

/**
 * @see org.gradle.api.internal.tasks.DefaultGroovySourceSet
 */
@Getter
public class DefaultAspectjSourceSet extends DefaultWeavingSourceSet implements AspectjSourceSet, HasPublicType {

    public DefaultAspectjSourceSet(ObjectFactory objectFactory, SourceSet sourceSet) {
        super(sourceSet, objectFactory);

        String name = sourceSet.getName();
        String displayName = ((DefaultSourceSet) sourceSet).getDisplayName();

        AspectjSourceDirectorySet aspectj = new DefaultAspectjSourceDirectorySet(objectFactory.sourceDirectorySet("aspectj", displayName + " AspectJ source"));
        aspectj.getFilter().include("**/*.java", "**/*.aj");
        SourceDirectorySet allAspectj = objectFactory.sourceDirectorySet("all" + name, displayName + " AspectJ source");
        allAspectj.source(aspectj);
        allAspectj.getFilter().include("**/*.aj");

        sourceSet.getExtensions().add("aspectj", aspectj);
        sourceSet.getExtensions().add("allAspectj", allAspectj);
    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(AspectjSourceSet.class);
    }
}
