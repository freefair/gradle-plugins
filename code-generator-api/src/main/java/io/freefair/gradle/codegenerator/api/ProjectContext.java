package io.freefair.gradle.codegenerator.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter(AccessLevel.PACKAGE)
@AllArgsConstructor
public class ProjectContext implements Serializable {
	private File rootDir;
	private File inputDir;
	private File outputDir;

	@Getter(AccessLevel.PACKAGE)
	private Map<String, Object> configurationValues;

	private String sourceSet;

	public String getNamespaceFromFile(File file) {
		String absolutePath = file.getAbsolutePath();
		if (absolutePath.startsWith(inputDir.getAbsolutePath()))
			absolutePath = absolutePath.replace(inputDir.getAbsolutePath(), "");
		else if (absolutePath.startsWith(outputDir.getAbsolutePath()))
			absolutePath = absolutePath.replace(outputDir.getAbsolutePath(), "");
		return absolutePath.substring(1).replace("/", ".");
	}

	public List<ProjectFile> getAllFiles() {
		return getAllFiles("", inputDir);
	}

	private List<ProjectFile> getAllFiles(String prefix, File directory) {
		List<ProjectFile> result = new ArrayList<>();
		if (directory.isDirectory()) {
			File[] array = directory.listFiles();
			if (array != null) {
				result.addAll(Arrays.stream(array).filter(File::isDirectory)
						.flatMap(f -> getAllFiles(prefix + f.getName() + ".", f).stream()).collect(Collectors.toList()));
				result.addAll(Arrays.stream(array).filter(File::isFile)
						.map(f -> new ProjectFile(prefix, f.getName(), this)).collect(Collectors.toList()));
			}
		}
		return result;
	}

	public byte[] readFileToByteArray(String namespace, String filename) throws IOException {
		return FileUtils.readFileToByteArray(buildFileFromParts(inputDir, namespace, filename));
	}

	public String readFile(String namespace, String filename) throws IOException {
		return readFile(namespace, filename, "UTF-8");
	}

	public String readFile(String namespace, String filename, String encoding) throws IOException {
		return FileUtils.readFileToString(buildFileFromParts(inputDir, namespace, filename), encoding);
	}

	public void writeOutputFile(String namespace, String filename, String content) throws IOException {
		writeOutputFile(namespace, filename, content.getBytes());
	}

	public void writeOutputFile(String namespace, String filename, byte[] content) throws IOException {
		File file = buildFileFromParts(outputDir, namespace, filename);
		boolean mkdirs = file.getParentFile().exists() || file.getParentFile().mkdirs();
		if (!mkdirs) throw new IOException("Cannot create directory " + file.getParent());
		FileUtils.writeByteArrayToFile(file, content);
	}

	private File buildFileFromParts(String namespace, String filename) {
		return new File(namespace.replace(".", "/"), filename);
	}

	private File buildFileFromParts(File root, String namespace, String filename) {
		return new File(root, buildFileFromParts(namespace, filename).getPath());
	}

	public boolean configurationExists(String key) {
		return configurationValues.containsKey(key);
	}

	public Object getConfigurationValue(String key) {
		return configurationValues.get(key);
	}

	public <T> T getConfigurationValueAs(String key, Class<T> clazz) {
		Object data = configurationValues.get(key);
		if(clazz.isAssignableFrom(data.getClass()))
			return (T) data;
		throw new RuntimeException("Could not transform value of type " + data.getClass().getCanonicalName() + " to " + clazz.getCanonicalName());
	}
}
