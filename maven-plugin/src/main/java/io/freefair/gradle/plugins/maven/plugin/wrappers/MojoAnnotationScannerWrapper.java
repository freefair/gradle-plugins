package io.freefair.gradle.plugins.maven.plugin.wrappers;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.maven.tools.plugin.extractor.ExtractionException;
import org.apache.maven.tools.plugin.extractor.annotations.scanner.MojoAnnotatedClass;
import org.apache.maven.tools.plugin.extractor.annotations.scanner.MojoAnnotationsScanner;
import org.apache.maven.tools.plugin.extractor.annotations.scanner.MojoAnnotationsScannerRequest;
import org.gradle.api.file.FileCollection;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Lars Grefer
 */
@RequiredArgsConstructor
@Setter
public class MojoAnnotationScannerWrapper implements MojoAnnotationsScanner {

    private final MojoAnnotationsScanner delegate;

    private FileCollection sourceDirectories;
    private FileCollection classesDirectories;

    @Override
    public Map<String, MojoAnnotatedClass> scan(MojoAnnotationsScannerRequest request) throws ExtractionException {

        request.setClassesDirectories(new ArrayList<>(classesDirectories.getFiles()));
        request.setSourceDirectories(new ArrayList<>(sourceDirectories.getFiles()));

        return delegate.scan(request);
    }
}
