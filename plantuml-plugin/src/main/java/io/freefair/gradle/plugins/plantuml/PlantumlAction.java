package io.freefair.gradle.plugins.plantuml;

import lombok.SneakyThrows;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceFileReader;
import org.gradle.workers.WorkAction;

/**
 * @author Lars Grefer
 */
public abstract class PlantumlAction implements WorkAction<PlantumlParameters> {

    @SneakyThrows
    @Override
    public void execute() {

        FileFormat fileFormat = getParameters().getFileFormat()
                .map(String::toUpperCase)
                .map(FileFormat::valueOf)
                .getOrElse(FileFormat.PNG);

        FileFormatOption fileFormatOption = new FileFormatOption(fileFormat, getParameters().getWithMetadata().get());
        SourceFileReader sourceFileReader = new SourceFileReader(
                getParameters().getInputFile().getAsFile().get(),
                getParameters().getOutputDirectory().getAsFile().get(),
                fileFormatOption
        );

        sourceFileReader.getGeneratedImages();
    }
}
