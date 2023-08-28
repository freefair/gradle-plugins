package io.freefair.gradle.plugins.aspectj.internal;

import io.freefair.gradle.plugins.aspectj.AspectjSourceDirectorySet;
import io.freefair.gradle.plugins.aspectj.AspectjSourceSet;
import lombok.Getter;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.tasks.DefaultSourceSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.SourceSet;

/**
 * @see org.gradle.api.internal.tasks.DefaultGroovySourceSet
 */
@Getter
public class DefaultAspectjSourceSet implements AspectjSourceSet, HasPublicType {

    public DefaultAspectjSourceSet(ObjectFactory objectFactory, SourceSet sourceSet) {
        String name = sourceSet.getName();
        String displayName = ((DefaultSourceSet) sourceSet).getDisplayName();

        AspectjSourceDirectorySet aspectj = createAspectjSourceDirectorySet(name, displayName, objectFactory);
        aspectj.getFilter().include("**/*.java", "**/*.aj");
        SourceDirectorySet allAspectj = objectFactory.sourceDirectorySet("all" + name, displayName + " AspectJ source");
        allAspectj.source(aspectj);
        allAspectj.getFilter().include("**/*.aj");

        sourceSet.getExtensions().add("aspectj", aspectj);
        sourceSet.getExtensions().add("allAspectj", allAspectj);
    }

    private static AspectjSourceDirectorySet createAspectjSourceDirectorySet(String name, String displayName, ObjectFactory objectFactory) {
        AspectjSourceDirectorySet aspectjSourceDirectorySet = objectFactory.newInstance(DefaultAspectjSourceDirectorySet.class, objectFactory.sourceDirectorySet(name, displayName + " AspectJ source"));
        aspectjSourceDirectorySet.getFilter().include("**/*.java", "**/*.aj");
        return aspectjSourceDirectorySet;
    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(AspectjSourceSet.class);
    }
}
