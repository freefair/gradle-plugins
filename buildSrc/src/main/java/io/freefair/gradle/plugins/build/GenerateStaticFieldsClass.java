package io.freefair.gradle.plugins.build;

import com.palantir.javapoet.*;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.annotation.processing.Generated;
import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;

@CacheableTask
public abstract class GenerateStaticFieldsClass extends DefaultTask {

    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();

    @Input
    public abstract Property<String> getClassName();

    @Input
    public abstract Property<String> getPackageName();

    @Input
    public abstract MapProperty<String, String> getFieldValues();

    @OutputDirectory
    public abstract DirectoryProperty getDestinationDirectory();

    @TaskAction
    public void generateStaticFields() throws IOException {

        getFileSystemOperations().delete(deleteSpec -> deleteSpec.delete(getDestinationDirectory()));

        TypeSpec typeSpec = TypeSpec.classBuilder(getClassName().get())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PRIVATE)
                        .build())
                .addFields(getFields())
                .build();

        JavaFile javaFile = JavaFile.builder(getPackageName().get(), typeSpec)
                .skipJavaLangImports(true)
                .build();

        javaFile.writeTo(getDestinationDirectory().get().getAsFile());


    }

    private Iterable<FieldSpec> getFields() {
        ArrayList<FieldSpec> fieldSpecs = new ArrayList<>();

        getFieldValues().get().forEach((key, value) -> {
            fieldSpecs.add(FieldSpec.builder(String.class, key)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", value).build());
        });

        return fieldSpecs;
    }
}
