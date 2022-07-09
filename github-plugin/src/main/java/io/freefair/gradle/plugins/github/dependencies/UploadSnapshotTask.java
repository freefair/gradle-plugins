package io.freefair.gradle.plugins.github.dependencies;


import com.google.gson.Gson;
import io.freefair.gradle.plugins.github.internal.GithubClient;
import io.freefair.gradle.plugins.github.internal.GithubService;
import io.freefair.gradle.plugins.github.internal.Snapshot;
import io.freefair.gradle.plugins.github.internal.UploadSnapshotResponse;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import retrofit2.Call;
import retrofit2.Response;

import javax.inject.Inject;
import java.io.FileReader;
import java.io.IOException;

public abstract class UploadSnapshotTask extends DefaultTask {

    private final GithubClient githubClient;

    @InputFile
    public abstract RegularFileProperty getSnapshotFile();

    @Input
    public abstract Property<String> getOwner();

    @Input
    public abstract Property<String> getRepo();

    @Inject
    public UploadSnapshotTask(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    @TaskAction
    public void upload() {
        GithubService githubService = githubClient.getGithubService();

        try (FileReader fileReader = new FileReader(getSnapshotFile().getAsFile().get())) {
            Snapshot snapshot = new Gson().fromJson(fileReader, Snapshot.class);
            getLogger().lifecycle("Job: {}", snapshot.getJob());
            getLogger().lifecycle("Ref: {}, Sha: {}", snapshot.getRef(), snapshot.getSha());
            getLogger().info("Detector: {}", snapshot.getDetector());

            Call<UploadSnapshotResponse> stringCall = githubService.uploadDependencySnapshot(getOwner().get(), getRepo().get(), snapshot);

            Response<UploadSnapshotResponse> execute = stringCall.execute();
            if (execute.isSuccessful()) {
                getLogger().lifecycle(execute.body().getMessage());
            }
            else {
                getLogger().error("{} {}", execute.code(), execute.message());
                getLogger().error(execute.errorBody().string());
                throw new RuntimeException("Upload Failed");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
