package io.freefair.gradle.plugins.mkdocs;

import io.freefair.gradle.plugins.mkdocs.tasks.MkDocsBuild;
import io.freefair.gradle.plugins.mkdocs.tasks.MkDocsNew;
import io.freefair.gradle.plugins.mkdocs.tasks.MkDocsServe;
import lombok.SneakyThrows;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class MkDocsPlugin implements Plugin<Project> {

    @SneakyThrows
    @Override
    public void apply(Project project) {

        project.getTasks().register("mkdocs", MkDocsBuild.class, mkDocs -> {
            mkDocs.setGroup("documentation");
            mkDocs.getConfigFile().convention(project.getLayout().getProjectDirectory().file("mkdocs.yml"));
            mkDocs.getSiteDir().convention(project.getLayout().getBuildDirectory().dir("docs/mkdocs"));
        });

        project.getTasks().register("mkdocsNew", MkDocsNew.class, mkDocsNew -> {
            mkDocsNew.setGroup("documentation");
            mkDocsNew.getProjectDirectory().convention(project.getLayout().getProjectDirectory());
        });

        project.getTasks().register("mkdocsServe", MkDocsServe.class, mkdocsServe -> {
            mkdocsServe.setGroup("documentation");
            mkdocsServe.getConfigFile().convention(project.getLayout().getProjectDirectory().file("mkdocs.yml"));
        });
    }
}
