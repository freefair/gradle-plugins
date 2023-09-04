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

    /**
     * The commit SHA associated with this dependency snapshot. Maximum length: 40 characters.
     */
    @Input
    public abstract Property<String> getSha();

    /**
     * The repository branch that triggered this snapshot.
     *
     * @return The repository branch that triggered this snapshot.
     */
    @Input
    public abstract Property<String> getRef();

    /**
     * The external ID of the job.
     *
     * @return The external ID of the job.
     */
    @Input
    @Optional
    public abstract Property<String> getJobId();

    /**
     * Correlator provides a key that is used to group snapshots submitted over time.
     * Only the "latest" submitted snapshot for a given combination of job.correlator and detector.name will be considered when calculating a repository's current dependencies.
     * Correlator should be as unique as it takes to distinguish all detection runs for a given "wave" of CI workflow you run.
     * If you're using GitHub Actions, a good default value for this could be the environment variables GITHUB_WORKFLOW and GITHUB_JOB concatenated together.
     * If you're using a build matrix, then you'll also need to add additional key(s) to distinguish between each submission inside a matrix variation.
     */
    @Input
    public abstract Property<String> getJobCorrelator();

    /**
     * The url for the job.
     *
     * @return The url for the job.
     */
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
