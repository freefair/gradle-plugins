package io.freefair.gradle.plugins.lombok.tasks;

import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;

public interface LombokTask extends Task {

    ConfigurableFileCollection getLombokClasspath();
}
