package io.freefair.gradle.plugins.plantuml;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkParameters;

/**
 * {@link WorkParameters} for {@link PlantumlAction}.
 *
 * @author Lars Grefer
 * @see PlantumlPlugin
 */
public interface PlantumlParameters extends WorkParameters {

    RegularFileProperty getInputFile();

    DirectoryProperty getOutputDirectory();

    Property<String> getFileFormat();

    Property<Boolean> getWithMetadata();
}
