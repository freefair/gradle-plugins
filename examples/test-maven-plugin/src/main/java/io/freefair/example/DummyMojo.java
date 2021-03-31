package io.freefair.example;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;

@Mojo(name = "test-dummy")
public class DummyMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Hello World.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}")
    private File outputDirectory;
}
