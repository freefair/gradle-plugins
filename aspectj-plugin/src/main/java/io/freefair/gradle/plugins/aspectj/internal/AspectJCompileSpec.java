package io.freefair.gradle.plugins.aspectj.internal;

import io.freefair.gradle.plugins.aspectj.AspectJCompileOptions;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.tasks.compile.DefaultJvmLanguageCompileSpec;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.jvm.toolchain.JavaLauncher;

import javax.annotation.Nullable;


@Getter
@Setter
public class AspectJCompileSpec extends DefaultJvmLanguageCompileSpec {

    private FileCollection aspectJClasspath;

    AspectJCompileOptions aspectJCompileOptions;

    private FileCollection additionalInpath;

    @Nullable
    private JavaLauncher launcher;
}
