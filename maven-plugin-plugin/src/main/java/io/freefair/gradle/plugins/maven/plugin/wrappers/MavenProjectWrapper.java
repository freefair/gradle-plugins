package io.freefair.gradle.plugins.maven.plugin.wrappers;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link MavenProject} implementation backed by a {@link Project gradle project}.
 *
 * @author Lars Grefer
 */
public class MavenProjectWrapper extends MavenProject {

    private final Project project;
    private final File pomFile;

    private final SourceSet main;
    private final SourceSet test;

    public MavenProjectWrapper(Project project, File pomFile) throws IOException, XmlPullParserException {
        this.project = project;
        this.pomFile = pomFile;

        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader(pomFile));

        setModel(model);

        getBuild().setDirectory(project.getBuildDir().getAbsolutePath());

        SourceSetContainer sourceSets = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();

        main = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        getBuild().setSourceDirectory(main.getJava().getSrcDirs().iterator().next().getAbsolutePath());
        getBuild().setOutputDirectory(main.getJava().getOutputDir().getAbsolutePath());

        test = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME);
        getBuild().setTestSourceDirectory(test.getJava().getSrcDirs().iterator().next().getAbsolutePath());
        getBuild().setTestOutputDirectory(test.getJava().getOutputDir().getAbsolutePath());

        setArtifact(new ProjectArtifact(this));
    }

    @Override
    public File getFile() {
        return pomFile;
    }

    @Override
    public File getBasedir() {
        return project.getProjectDir();
    }

    @Override
    public List<String> getCompileSourceRoots() {
        return main.getJava().getSrcDirs().stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getTestCompileSourceRoots() {
        return test.getJava().getSrcDirs().stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());
    }
}
