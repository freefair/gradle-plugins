package io.freefair.gradle.plugins.lombok.tasks.internal;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.workers.WorkParameters;

public interface LombokConfigParameters extends WorkParameters {

    ListProperty<String> getArgs();

    RegularFileProperty getOutputFile();
}
