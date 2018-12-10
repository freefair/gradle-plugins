package io.freefair.gradle.plugins.lombok;


import io.freefair.gradle.plugins.AbstractPluginTest;
import io.freefair.gradle.plugins.builder.io.FileBuilder;
import io.freefair.gradle.plugins.builder.java.JavaClassBuilder;
import org.gradle.testkit.runner.BuildResult;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class DelombokTest extends AbstractPluginTest {

  @Test
  public void simpleDelombokTest() {
    createGradleConfiguration()
      .applyPlugin("java")
      .applyPlugin("io.freefair.lombok")
      .addCustomConfigurationBlock("delombok.target = file('src/main-delombok/java/')")
      .write();

    JavaClassBuilder javaClass = createJavaClass("main", "io.freefair.gradle.plugins.lombok.test", "SimpleLombokFile");
    javaClass.addAnnotation()
      .setName("lombok.Data");
    javaClass.addField()
      .setName("name").setType("String");
    javaClass.addField()
      .setName("firstName").setType("String");
    javaClass.addField()
      .setName("age").setType("int");
    javaClass.write();

    executeTask("build", "delombok", "--debug");
    String simpleLombokFile = readJavaClass("main-delombok", "io.freefair.gradle.plugins.lombok.test", "SimpleLombokFile");
    assertThat(simpleLombokFile, not(containsString("lombok.Data")));
  }
}
