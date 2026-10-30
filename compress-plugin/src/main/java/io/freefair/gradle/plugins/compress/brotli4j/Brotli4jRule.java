package io.freefair.gradle.plugins.compress.brotli4j;

import lombok.Data;
import org.gradle.api.artifacts.CacheableRule;
import org.gradle.api.artifacts.ComponentMetadataContext;
import org.gradle.api.artifacts.ComponentMetadataRule;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.MachineArchitecture;
import org.gradle.nativeplatform.OperatingSystemFamily;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;
import java.util.List;

@CacheableRule
public abstract class Brotli4jRule implements ComponentMetadataRule {

    @Inject
    protected abstract ObjectFactory getObjectFactory();

    private static final List<NativeVariant> nativeVariants = List.of(
            new NativeVariant(OperatingSystemFamily.WINDOWS, "aarch64", "windows-aarch64"),
            new NativeVariant(OperatingSystemFamily.WINDOWS, "x86-64", "windows-x86_64"),
            new NativeVariant(OperatingSystemFamily.MACOS, "x86-64", "osx-x86_64"),
            new NativeVariant(OperatingSystemFamily.MACOS, "aarch64", "osx-aarch64"),
            new NativeVariant(OperatingSystemFamily.LINUX, "x86-64", "linux-x86_64"),
            new NativeVariant(OperatingSystemFamily.LINUX, "aarch64", "linux-aarch64"),
            new NativeVariant(OperatingSystemFamily.LINUX, "arm-v7", "linux-armv7"),
            new NativeVariant(OperatingSystemFamily.LINUX, "s390x", "linux-s390x"),
            new NativeVariant(OperatingSystemFamily.LINUX, "riscv64", "linux-riscv64"),
            new NativeVariant(OperatingSystemFamily.LINUX, "ppc64le", "linux-ppc64le")
    );

    @Override
    public void execute(@NonNull ComponentMetadataContext context) {
        addVariant(context, "compile");
        addVariant(context, "runtime");
    }

    private void addVariant(ComponentMetadataContext context, String base) {
        String version = context.getDetails().getId().getVersion();

        nativeVariants.forEach(nativeVariant -> {

            context.getDetails().addVariant(nativeVariant.classifier + "-" + base, base, variantMetadata -> {
                variantMetadata.getAttributes().attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, getObjectFactory().named(OperatingSystemFamily.class, nativeVariant.os));
                variantMetadata.getAttributes().attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, getObjectFactory().named(MachineArchitecture.class, nativeVariant.arch));

                variantMetadata.withDependencies(deps -> {
                    deps.add(nativeVariant.dependency(version));
                });
            });

        });

        context.getDetails().addVariant("all-" + base, base, variantMetadata -> {
            variantMetadata.getAttributes().attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, getObjectFactory().named(OperatingSystemFamily.class, "all"));
            variantMetadata.getAttributes().attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, getObjectFactory().named(MachineArchitecture.class, "all"));

            variantMetadata.withDependencies(deps -> {
                nativeVariants.forEach(nativeVariant -> {
                    deps.add(nativeVariant.dependency(version));
                });
            });
        });
    }

    @Data
    private static class NativeVariant {
        private final String os;
        private final String arch;
        private final String classifier;

        public String dependency(String version) {
            return "com.aayushatharva.brotli4j:native-%s:%s".formatted(classifier, version);
        }
    }
}
