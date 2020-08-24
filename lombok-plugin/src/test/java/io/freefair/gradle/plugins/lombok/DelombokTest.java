package io.freefair.gradle.plugins.lombok;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.freefair.gradle.plugins.AbstractPluginTest;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

public class DelombokTest extends AbstractPluginTest {

    @Test
    public void simpleDelombokTest() {
        createGradleConfiguration()
                .applyPlugin("java")
                .applyPlugin("io.freefair.lombok")
                .addCustomConfigurationBlock("delombok.target = file('src/main-delombok/java/')")
                .write();

        TypeSpec.Builder builder = TypeSpec.classBuilder("SimpleLombokFile");
        builder.addField(FieldSpec.builder(String.class, "name", Modifier.PRIVATE).build());
        builder.addField(FieldSpec.builder(String.class, "firstName", Modifier.PRIVATE).build());
        builder.addField(FieldSpec.builder(TypeName.INT, "age", Modifier.PRIVATE).build());
        builder.addAnnotation(AnnotationSpec.builder(lombok.Data.class).build());

        createJavaClass("main", "io.freefair.gradle.plugins.lombok.test", builder.build());

        executeTask("build", "delombok", "--debug");
        String simpleLombokFile = readJavaClass("main-delombok", "io.freefair.gradle.plugins.lombok.test", "SimpleLombokFile");
        assertThat(simpleLombokFile).doesNotContain("lombok.Data");
    }
}
