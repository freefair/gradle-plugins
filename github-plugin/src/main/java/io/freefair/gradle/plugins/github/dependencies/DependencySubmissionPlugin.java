package io.freefair.gradle.plugins.github.dependencies;

import com.google.gson.Gson;
import io.freefair.gradle.plugins.github.GithubBasePlugin;
import io.freefair.gradle.plugins.github.internal.GithubService;
import io.freefair.gradle.plugins.github.internal.Snapshot;
import io.freefair.gradle.plugins.github.internal.UploadSnapshotResponse;
import io.freefair.gradle.util.GitUtil;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;
import retrofit2.Call;
import retrofit2.Response;

import java.io.FileReader;
import java.io.IOException;

public class DependencySubmissionPlugin implements Plugin<Project> {

    private TaskProvider<DependencySnapshotTask> githubDependencySnapshot;

    @Override
    public void apply(Project project) {

        GithubBasePlugin basePlugin = project.getPlugins().apply(GithubBasePlugin.class);

        githubDependencySnapshot = project.getTasks().register("githubDependencySnapshot", DependencySnapshotTask.class, gds -> {
            gds.getSha().convention(project.provider(() -> GitUtil.getSha(project)));
            gds.getRef().convention(project.provider(() -> GitUtil.getRef(project)));
            gds.getOutputFile().convention(project.getLayout().getBuildDirectory().file("github/dependency-snapshot.json"));

            gds.getJobCorrelator().convention(project.getName());

            if (GitUtil.isGithubActions()) {
                gds.getJobId().set(System.getenv("GITHUB_RUN_ID"));
                gds.getJobCorrelator().set(
                        String.format("%s_%s_%s", System.getenv("GITHUB_WORKFLOW"), System.getenv("GITHUB_JOB"), project.getName())
                );
                String htmlUrl = String.format(
                        "%s/%s/actions/runs/%s",
                        System.getenv("GITHUB_SERVER_URL"),
                        System.getenv("GITHUB_REPOSITORY"),
                        System.getenv("GITHUB_RUN_ID")
                );
                gds.getJobHtmlUrl().set(htmlUrl);
            }
            else if (GitUtil.isTravisCi()) {
                gds.getJobId().set(System.getenv("TRAVIS_JOB_ID"));
                gds.getJobCorrelator().set(String.format("%s_%s", System.getenv("TRAVIS_JOB_NAME"), project.getName()));
                gds.getJobHtmlUrl().set(System.getenv("TRAVIS_JOB_WEB_URL"));
            }
        });

        project.allprojects(this::configureProject);

        project.getTasks().register("uploadGithubDependenciesSnapshot", t -> {
            t.dependsOn(githubDependencySnapshot);
            t.getInputs().file(githubDependencySnapshot.map(DependencySnapshotTask::getOutputFile));
            t.doLast(t2 -> {
                GithubService githubService = basePlugin.getGithubClient().getGithubService();

                Snapshot snapshot;
                try (FileReader fileReader = new FileReader(githubDependencySnapshot.get().getOutputFile().getAsFile().get())) {
                    snapshot = new Gson().fromJson(fileReader, Snapshot.class);
                    Call<UploadSnapshotResponse> stringCall = githubService.uploadDependencySnapshot(basePlugin.getGithubExtension().getOwner().get(), basePlugin.getGithubExtension().getRepo().get(), snapshot);

                    Response<UploadSnapshotResponse> execute = stringCall.execute();
                    t.getLogger().lifecycle(execute.body().getMessage());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
        });
    }

    private void configureProject(Project project) {

        DependencyManifestPlugin manifestPlugin = project.getPlugins().apply(DependencyManifestPlugin.class);

        githubDependencySnapshot.configure(gds -> {
            gds.source(manifestPlugin.getManifestTaskProvider());
        });

    }
}
