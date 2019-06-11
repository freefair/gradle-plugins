package io.freefair.gradle.plugins.aspectj.internal;

import io.freefair.gradle.plugins.aspectj.AspectJCompileOptions;
import lombok.Data;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.tasks.compile.DefaultJvmLanguageCompileSpec;


@Data
public class AspectJCompileSpec extends DefaultJvmLanguageCompileSpec {

    private FileCollection aspectJClasspath;

    AspectJCompileOptions aspectJCompileOptions;
}
