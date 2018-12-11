package io.freefair.gradle.codegenerator.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@Getter
@Setter(AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ProjectFile {
	private String namespace;
	private String filename;

	@Getter(AccessLevel.PACKAGE)
	private ProjectContext context;

	public String read() throws IOException {
		return context.readFile(namespace, filename);
	}

	public String read(String encoding) throws IOException {
		return context.readFile(namespace, filename, encoding);
	}
}
