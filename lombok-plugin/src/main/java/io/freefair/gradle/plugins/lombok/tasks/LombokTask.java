package io.freefair.gradle.plugins.lombok.tasks;

import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.Classpath;

public interface LombokTask extends Task {

    @Classpath
    ConfigurableFileCollection getLombokClasspath();
}
