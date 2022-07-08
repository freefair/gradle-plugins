package io.freefair.gradle.plugins.github.dependencies;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.freefair.gradle.plugins.github.internal.Detector;
import io.freefair.gradle.plugins.github.internal.Job;
import io.freefair.gradle.plugins.github.internal.Manifest;
import io.freefair.gradle.plugins.github.internal.Snapshot;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.UUID;

public abstract class DependencySnapshotTask extends SourceTask {

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @Input
    public abstract Property<String> getSha();

    @Input
    public abstract Property<String> getRef();

    @Input
    @Optional
    public abstract Property<String> getJobId();

    @Input
    public abstract Property<String> getJobCorrelator();
    @Input
    @Optional
    public abstract Property<String> getJobHtmlUrl();

    @Override
    @PathSensitive(PathSensitivity.NONE)
    public FileTree getSource() {
        return super.getSource();
    }

    @TaskAction
    public void buildSnapshot() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Snapshot snapshot = new Snapshot();

        snapshot.setSha(getSha().get());
        snapshot.setRef(getRef().get());
        snapshot.setDetector(new Detector());
        snapshot.setManifests(new LinkedHashMap<>());
        snapshot.setScanned(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());

        snapshot.setJob(new Job());
        snapshot.getJob().setId(getJobId().getOrElse(UUID.randomUUID().toString()));
        snapshot.getJob().setCorrelator(getJobCorrelator().get());
        snapshot.getJob().setHtml_url(getJobHtmlUrl().getOrNull());

        for (File file : getSource()) {
            try (FileReader fileReader = new FileReader(file)) {
                Manifest manifest = gson.fromJson(fileReader, Manifest.class);
                snapshot.getManifests().put(manifest.getName(), manifest);
            }
        }

        String json = gson.toJson(snapshot);
        Files.write(getOutputFile().getAsFile().get().toPath(), json.getBytes(StandardCharsets.UTF_8));
    }
}
