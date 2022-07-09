package io.freefair.gradle.plugins.github.dependencies;

import io.freefair.gradle.plugins.github.GithubBasePlugin;
import io.freefair.gradle.util.GitUtil;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

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
            else if (GitUtil.isCircleCi()) {
                gds.getJobId().set(System.getenv("CIRCLE_WORKFLOW_JOB_ID"));
                gds.getJobCorrelator().set(String.format("%s_%s", System.getenv("CIRCLE_JOB"), project.getName()));
                gds.getJobCorrelator().set(String.format("%s_%s", System.getenv("CIRCLE_BUILD_URL"), project.getName()));
            }
        });

        project.allprojects(this::configureProject);

        TaskProvider<UploadSnapshotTask> uploadGithubDependenciesSnapshot = project.getTasks().register("uploadGithubDependenciesSnapshot", UploadSnapshotTask.class, basePlugin.getGithubClient());
        uploadGithubDependenciesSnapshot.configure(t -> {
            t.getSnapshotFile().set(githubDependencySnapshot.flatMap(DependencySnapshotTask::getOutputFile));

            t.getOwner().convention(basePlugin.getGithubExtension().getOwner());
            t.getRepo().convention(basePlugin.getGithubExtension().getRepo());
        });
    }

    private void configureProject(Project project) {

        DependencyManifestPlugin manifestPlugin = project.getPlugins().apply(DependencyManifestPlugin.class);

        githubDependencySnapshot.configure(gds -> {
            gds.source(manifestPlugin.getManifestTaskProvider());
        });

    }
}
