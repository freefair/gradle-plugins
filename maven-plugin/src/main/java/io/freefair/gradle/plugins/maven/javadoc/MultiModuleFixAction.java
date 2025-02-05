package io.freefair.gradle.plugins.maven.javadoc;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.gradle.external.javadoc.JavadocOptionFileOption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @author Lars Grefer
 * @see <a href="https://github.com/freefair/gradle-plugins/issues/409">#409</a>
 */
public class MultiModuleFixAction implements Action<Task> {

    private final Pattern moduleNamePattern = Pattern.compile("module\\s+(.*?)\\s*\\{");

    @Override
    public void execute(@Nonnull Task task) {
        Javadoc javadoc = (Javadoc) task;

        Set<File> source = javadoc.getSource().getFiles();

        List<File> moduleInfoFiles = source.stream()
                .filter(file -> file.getName().startsWith("module-info.java"))
                .collect(Collectors.toList());

        if (moduleInfoFiles.isEmpty()) {
            return;
        }

        CoreJavadocOptions options = (CoreJavadocOptions) javadoc.getOptions();
        JavadocOptionFileOption<List<String>> moduleSourcePath = options.addMultilineStringsOption("-module-source-path");

        Charset charset = StandardCharsets.UTF_8;

        if (options.getEncoding() != null) {
            charset = Charset.forName(options.getEncoding());
        }

        for (File moduleInfoFile : moduleInfoFiles) {

            try {
                String moduleName = getModuleName(moduleInfoFile, charset);

                moduleSourcePath.getValue().add(moduleName + "=" + moduleInfoFile.getParentFile().getAbsolutePath());

            } catch (IOException e) {
                javadoc.getLogger().warn("Failed to read module info file: {}", moduleInfoFile.getAbsolutePath(), e);
            }

        }

    }

    @Nullable
    private String getModuleName(File moduleInfoFile, Charset charset) throws IOException {

        byte[] bytes = Files.readAllBytes(moduleInfoFile.toPath());
        String content = new String(bytes, charset);

        Matcher matcher = moduleNamePattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new IllegalArgumentException("Module name not found: " + moduleInfoFile.getAbsolutePath());
    }


}
