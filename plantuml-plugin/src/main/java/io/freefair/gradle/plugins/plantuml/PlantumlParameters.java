package io.freefair.gradle.plugins.plantuml;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkParameters;

/**
 * @author Lars Grefer
 */
public interface PlantumlParameters extends WorkParameters {

    RegularFileProperty getInputFile();

    DirectoryProperty getOutputDirectory();

    Property<String> getFileFormat();

    Property<Boolean> getWithMetadata();
}
