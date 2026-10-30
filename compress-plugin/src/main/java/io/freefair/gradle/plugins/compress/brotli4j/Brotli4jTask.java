package io.freefair.gradle.plugins.compress.brotli4j;

import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.Classpath;

public interface Brotli4jTask extends Task {

    @Classpath
    ConfigurableFileCollection getBrotli4jClasspath();
}
