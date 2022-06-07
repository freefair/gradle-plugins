package io.freefair.gradle.plugins.lombok.tasks;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.jvm.toolchain.JavaToolchainSpec;

public interface LombokTask extends Task {

    @Classpath
    ConfigurableFileCollection getLombokClasspath();

    /**
     * The {@link JavaLauncher} which will be used to invoke lombok.
     *
     * @see JavaToolchainService#launcherFor(JavaToolchainSpec)
     * @see JavaToolchainService#launcherFor(Action)
     */
    @Nested
    @Optional
    Property<JavaLauncher> getLauncher();
}
