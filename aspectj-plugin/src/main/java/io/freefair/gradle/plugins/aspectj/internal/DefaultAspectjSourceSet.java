package io.freefair.gradle.plugins.aspectj.internal;

import groovy.lang.Closure;
import io.freefair.gradle.plugins.aspectj.AspectjSourceSet;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.tasks.DefaultSourceSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.SourceSet;
import org.gradle.util.ConfigureUtil;

import javax.annotation.Nullable;

/**
 * @see org.gradle.api.internal.tasks.DefaultGroovySourceSet
 */
@Getter
public class DefaultAspectjSourceSet extends DefaultWeavingSourceSet implements AspectjSourceSet, HasPublicType {


    private final SourceDirectorySet aspectj;
    private final SourceDirectorySet allAspectj;

    public DefaultAspectjSourceSet(ObjectFactory objectFactory, SourceSet sourceSet) {
        super(sourceSet);

        String name = sourceSet.getName();
        String displayName = ((DefaultSourceSet) sourceSet).getDisplayName();

        aspectj = objectFactory.sourceDirectorySet(name, displayName + " AspectJ source");
        aspectj.getFilter().include("**/*.java", "**/*.aj");
        allAspectj = objectFactory.sourceDirectorySet("all" + name, displayName + " AspectJ source");
        allAspectj.source(aspectj);
        allAspectj.getFilter().include("**/*.aj");
    }


    @Override
    public AspectjSourceSet aspectj(@Nullable Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, getAspectj());
        return this;
    }

    @Override
    public AspectjSourceSet aspectj(Action<? super SourceDirectorySet> configureAction) {
        configureAction.execute(getAspectj());
        return this;
    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(AspectjSourceSet.class);
    }
}
