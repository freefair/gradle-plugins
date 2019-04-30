package io.freefair.gradle.plugins.lombok.tasks;

import lombok.Getter;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import javax.annotation.Nonnull;

@Getter
public abstract class LombokJarTask extends AbstractArchiveTask implements LombokTask {

    @Classpath
    private final ConfigurableFileCollection lombokClasspath = getProject().files();

    LombokJarTask() {
        getArchiveExtension().set("jar");
        getArchiveBaseName().convention("lombok");
    }

    @Override
    @Nonnull
    protected final CopyAction createCopyAction() {
        throw new UnsupportedOperationException();
    }
}
