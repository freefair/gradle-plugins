package io.freefair.gradle.plugins.mkdocs;

import io.freefair.gradle.plugins.mkdocs.tasks.MkDocsBuild;
import io.freefair.gradle.plugins.mkdocs.tasks.MkDocsNew;
import io.freefair.gradle.plugins.mkdocs.tasks.MkDocsServe;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.BasePluginExtension;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Tar;
import org.gradle.api.tasks.bundling.Zip;

/**
 * Gradle plugin for MkDocs documentation generator.
 * <p>
 * Provides tasks to build, serve, and package MkDocs-based documentation.
 * Requires MkDocs to be installed and available on the system PATH.
 * <p>
 * This plugin registers the following tasks:
 * <ul>
 *   <li>{@code mkdocs} - Builds the documentation</li>
 *   <li>{@code mkdocsServe} - Starts the development server</li>
 *   <li>{@code mkdocsNew} - Creates a new MkDocs project</li>
 *   <li>{@code mkdocsZip} - Creates a ZIP archive of the built documentation</li>
 *   <li>{@code mkdocsTar} - Creates a TAR archive of the built documentation</li>
 * </ul>
 */
public class MkDocsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        TaskProvider<MkDocsBuild> mkdocs = project.getTasks().register("mkdocs", MkDocsBuild.class, mkDocs -> {
            mkDocs.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
            mkDocs.getConfigFile().convention(project.getLayout().getProjectDirectory().file("mkdocs.yml"));
            mkDocs.getSiteDir().convention(project.getLayout().getBuildDirectory().dir("docs/mkdocs"));
        });

        TaskProvider<Zip> mkdocsZip = project.getTasks().register("mkdocsZip", Zip.class, zip -> {
            zip.from(mkdocs);
        });

        TaskProvider<Tar> mkdocsTar = project.getTasks().register("mkdocsTar", Tar.class, tar -> {
            tar.from(mkdocs);
        });

        project.getPlugins().withType(BasePlugin.class, basePlugin -> {
            BasePluginExtension baseExtension = project.getExtensions().getByType(BasePluginExtension.class);
            DirectoryProperty distsDirectory = baseExtension.getDistsDirectory();
            mkdocsZip.configure(zip -> zip.getDestinationDirectory().convention(distsDirectory));
            mkdocsTar.configure(tar -> tar.getDestinationDirectory().convention(distsDirectory));
        });

        project.getTasks().register("mkdocsNew", MkDocsNew.class, mkDocsNew -> {
            mkDocsNew.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
            mkDocsNew.getProjectDirectory().convention(project.getLayout().getProjectDirectory());
        });

        project.getTasks().register("mkdocsServe", MkDocsServe.class, mkdocsServe -> {
            mkdocsServe.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
            mkdocsServe.getConfigFile().convention(project.getLayout().getProjectDirectory().file("mkdocs.yml"));
        });
    }
}
