package io.freefair.gradle.plugins.github.dependencies;

import io.freefair.gradle.plugins.github.GithubBasePlugin;
import io.freefair.gradle.util.GitUtil;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public abstract class DependencySubmissionPlugin implements Plugin<Project> {

    @Inject
    protected abstract ProviderFactory getProviderFactory();

    private TaskProvider<DependencySnapshotTask> githubDependencySnapshot;

    @Override
    public void apply(Project project) {

        GithubBasePlugin basePlugin = project.getPlugins().apply(GithubBasePlugin.class);

        githubDependencySnapshot = project.getTasks().register("githubDependencySnapshot", DependencySnapshotTask.class, gds -> {
            gds.getSha().convention(GitUtil.getSha(project));
            gds.getRef().convention(GitUtil.getRef(project));
            gds.getOutputFile().convention(project.getLayout().getBuildDirectory().file("github/dependency-snapshot.json"));

            Transformer<String, String> addProjectName = base -> base + "_" + project.getName();

            if (GitUtil.isGithubActions(getProviderFactory())) {
                gds.getJobId().set(getProviderFactory().environmentVariable("GITHUB_RUN_ID"));
                String jobCorrelator = String.format(
                        "%s_%s_%s",
                        getProviderFactory().environmentVariable("GITHUB_WORKFLOW").get(),
                        getProviderFactory().environmentVariable("GITHUB_JOB").get(),
                        project.getName());
                gds.getJobCorrelator().set(jobCorrelator);
                String htmlUrl = String.format(
                        "%s/%s/actions/runs/%s",
                        getProviderFactory().environmentVariable("GITHUB_SERVER_URL").get(),
                        getProviderFactory().environmentVariable("GITHUB_REPOSITORY").get(),
                        getProviderFactory().environmentVariable("GITHUB_RUN_ID").get()
                );
                gds.getJobHtmlUrl().set(htmlUrl);
            }
            else if (GitUtil.isTravisCi(getProviderFactory())) {
                gds.getJobId().set(getProviderFactory().environmentVariable("TRAVIS_JOB_ID"));
                gds.getJobCorrelator().set(getProviderFactory().environmentVariable("TRAVIS_JOB_NAME").map(addProjectName));
                gds.getJobHtmlUrl().set(getProviderFactory().environmentVariable("TRAVIS_JOB_WEB_URL"));
            }
            else if (GitUtil.isCircleCi(getProviderFactory())) {
                gds.getJobId().set(getProviderFactory().environmentVariable("CIRCLE_WORKFLOW_JOB_ID"));
                gds.getJobCorrelator().set(getProviderFactory().environmentVariable("CIRCLE_JOB").map(addProjectName));
                gds.getJobHtmlUrl().set(getProviderFactory().environmentVariable("CIRCLE_BUILD_URL"));
            }
            else if (GitUtil.isGitLab(getProviderFactory())) {
                gds.getJobId().set(getProviderFactory().environmentVariable("CI_JOB_ID"));
                gds.getJobCorrelator().set(getProviderFactory().environmentVariable("CI_JOB_NAME").map(addProjectName));
                gds.getJobHtmlUrl().set(getProviderFactory().environmentVariable("CI_JOB_URL"));
            }
            else {
                gds.getJobCorrelator().convention(project.getName());
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
