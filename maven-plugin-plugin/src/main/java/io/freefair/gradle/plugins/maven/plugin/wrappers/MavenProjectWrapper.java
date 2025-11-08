package io.freefair.gradle.plugins.maven.plugin.wrappers;

import io.freefair.gradle.plugins.maven.plugin.internal.MavenHelper;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link MavenProject} implementation backed by a {@link Project gradle project}.
 *
 * @author Lars Grefer
 */
public class MavenProjectWrapper extends MavenProject {

    private final ProjectLayout projectLayout;
    private final File pomFile;

    private FileCollection mainSourceDirs;

    public MavenProjectWrapper(ProjectLayout projectLayout, File pomFile) throws IOException, XmlPullParserException {
        this.projectLayout = projectLayout;
        this.pomFile = pomFile;

        Model model = MavenHelper.parsePom(pomFile);

        setModel(model);

        getBuild().setDirectory(projectLayout.getBuildDirectory().get().getAsFile().getAbsolutePath());

        setArtifact(new ProjectArtifact(this));
    }

    @Override
    public File getFile() {
        return pomFile;
    }

    @Override
    public File getBasedir() {
        return projectLayout.getProjectDirectory().getAsFile();
    }

    @Override
    public List<String> getCompileSourceRoots() {
        return mainSourceDirs.getFiles().stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());
    }

    public void setMainSourceDirs(FileCollection mainSourceDirs) {
        this.mainSourceDirs = mainSourceDirs;
        getBuild().setSourceDirectory(mainSourceDirs.getFiles().iterator().next().getAbsolutePath());
    }

}
