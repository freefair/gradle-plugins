package io.freefair.gradle.plugins.lombok;


import io.freefair.gradle.plugins.AbstractPluginTest;
import io.freefair.gradle.plugins.FileBuilder;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

public class DelombokTest extends AbstractPluginTest {

	@Test
	public void simpleDelombokTest() {
		createGradleConfiguration()
				.applyPlugin("java")
				.applyPlugin("io.freefair.lombok")
				.write();

		FileBuilder file =
				createFile("src/main/java/io/freefair/gradle/plugins/lombok/test", "SimpleLombokFile.java")
				.append("package io.freefair.gradle.plugins.lombok.test;").appendNewLine()
				.append("@Data").appendNewLine()
				.append("class SimpleLombokFile {").indent().appendNewLine()
				.append("private String name;").appendNewLine()
				.append("private String firstName;").appendNewLine()
				.append("private int age;").unindent()
				.appendNewLine().append("}");

		file.write();

		executeTask("delombok");
	}
}
