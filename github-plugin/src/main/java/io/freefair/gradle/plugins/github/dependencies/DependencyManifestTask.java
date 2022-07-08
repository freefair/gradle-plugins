package io.freefair.gradle.plugins.github.dependencies;

import com.github.packageurl.PackageURLBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.freefair.gradle.plugins.github.internal.GitUtils;
import io.freefair.gradle.plugins.github.internal.Manifest;
import lombok.SneakyThrows;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.internal.DefaultGradleVersion;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public abstract class DependencyManifestTask extends DefaultTask {

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @Input
    public abstract ListProperty<ResolvedComponentResult> getDevelopmentClasspaths();

    @Input
    public abstract ListProperty<ResolvedComponentResult> getRuntimeClasspaths();

    private transient Manifest manifest;

    @TaskAction
    public void writeManifest() throws IOException {

        manifest = new Manifest();

        manifest.setName(getProject().getPath());

        File gitDir = GitUtils.findWorkingDirectory(getProject());

        String filePath = getProject().getBuildFile().getCanonicalPath().replace(gitDir.getCanonicalPath(), "");
        if (filePath.startsWith(File.separator)) {
            filePath = filePath.substring(1);
        }
        manifest.setFile(new Manifest.File(filePath));

        addGradleDependency();

        for (ResolvedComponentResult developmentClasspath : getDevelopmentClasspaths().get()) {
            developmentClasspath.getDependencies().stream()
                    .filter(dep -> !dep.isConstraint())
                    .filter(ResolvedDependencyResult.class::isInstance)
                    .map(ResolvedDependencyResult.class::cast)
                    .map(resolvedDependencyResult -> getGithubDependency(resolvedDependencyResult.getSelected(), "development"))
                    .forEach(dep -> {
                        dep.setRelationship("direct");
                    });
        }

        for (ResolvedComponentResult runtimeClasspath : getRuntimeClasspaths().get()) {
            runtimeClasspath.getDependencies().stream()
                    .filter(dep -> !dep.isConstraint())
                    .filter(ResolvedDependencyResult.class::isInstance)
                    .map(ResolvedDependencyResult.class::cast)
                    .map(d -> getGithubDependency(d.getSelected(), "runtime"))
                    .forEach(dep -> {
                        dep.setRelationship("direct");
                    });
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(manifest);
        Files.write(getOutputFile().getAsFile().get().toPath(), json.getBytes(StandardCharsets.UTF_8));

    }

    private void addGradleDependency() {
        Manifest.Dependency gradleDep = new Manifest.Dependency(getPackageUrl(DefaultGradleVersion.current()));
        gradleDep.setScope("development");
        gradleDep.setRelationship("direct");
        manifest.getResolved().put(gradleDep.getPackage_url(), gradleDep);
    }

    @NotNull
    private Manifest.Dependency getGithubDependency(ResolvedComponentResult componentResult, String scope) {

        String packageUrl = getPackageUrl(componentResult);

        Manifest.Dependency githubDep = manifest.getResolved().computeIfAbsent(packageUrl, Manifest.Dependency::new);

        githubDep.setRelationship("indirect");
        githubDep.setScope(scope);

        List<String> collect = componentResult.getDependencies().stream()
                .filter(dep -> !dep.isConstraint())
                .filter(ResolvedDependencyResult.class::isInstance)
                .map(ResolvedDependencyResult.class::cast)
                .map(d -> getGithubDependency(d.getSelected(), scope))
                .map(Manifest.Dependency::getPackage_url)
                .collect(Collectors.toList());

        githubDep.setDependencies(collect);


        return githubDep;
    }

    @SneakyThrows
    private String getPackageUrl(ResolvedComponentResult selected) {
        return PackageURLBuilder.aPackageURL()
                .withType("maven")
                .withNamespace(selected.getModuleVersion().getGroup())
                .withName(selected.getModuleVersion().getName())
                .withVersion(selected.getModuleVersion().getVersion())
                .build()
                .toString();
    }

    @SneakyThrows
    private String getPackageUrl(DefaultGradleVersion gradleVersion) {
        return PackageURLBuilder.aPackageURL()
                .withType("github")
                .withNamespace("gradle")
                .withName("gradle")
                .withVersion(gradleVersion.getGitRevision())
                .build()
                .toString();
    }

}
