package io.freefair.gradle.codegenerator.api;

public interface Generator {
	void generate(ProjectContext context) throws Exception;
}
