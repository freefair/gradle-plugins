package io.freefair.gradle.plugins.maven.central;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.problems.ProblemGroup;
import org.gradle.api.problems.ProblemId;
import org.gradle.api.problems.Problems;
import org.gradle.api.problems.Severity;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.VerificationTask;

import javax.inject.Inject;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Lars Grefer
 */
public abstract class ValidateMavenPom extends DefaultTask implements VerificationTask {

    public static final ProblemGroup PROBLEM_GROUP = ProblemGroup.create("validate-maven-pom", "Maven Pom Validation");

    @Inject
    public abstract Problems getProblems();

    @InputFile
    public abstract RegularFileProperty getPomFile();

    @Input
    private final Property<Boolean> ignoreFailures = getProject().getObjects().property(Boolean.class).convention(false);

    @Getter(AccessLevel.NONE)
    private boolean errorFound = false;

    private final List<String> missingElements = new ArrayList<>();

    @TaskAction
    public void check() throws IOException, XmlPullParserException {

        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader(getPomFile().getAsFile().get()));
        missingElements.clear();

        if (isEmpty(model.getGroupId())) {
            logError("groupId");
        }
        if (isEmpty(model.getArtifactId())) {
            logError("artifactId");
        }
        if (isEmpty(model.getVersion())) {
            logError("version");
        }

        if (isEmpty(model.getName())) {
            logError("name");
        }
        if (isEmpty(model.getDescription())) {
            logError("description");
        }
        if (isEmpty(model.getUrl())) {
            logError("url");
        }

        if (isEmpty(model.getLicenses())) {
            logError("licenses");
        }
        else {
            model.getLicenses().forEach(license -> {
                if (isEmpty(license.getName())) {
                    logError("license.name");
                }
                if (isEmpty(license.getUrl())) {
                    logError("license.url");
                }
            });
        }

        if (isEmpty(model.getDevelopers())) {
            logError("developers");
        }
        else {
            model.getDevelopers().forEach(developer -> {
                if (isEmpty(developer.getId())) {
                    logError("developer.id");
                }
                if (isEmpty(developer.getName())) {
                    logError("developer.name");
                }
                if (isEmpty(developer.getEmail())) {
                    logError("developer.email");
                }
            });
        }

        if (model.getScm() == null) {
            logError("scm");
        }
        else {
            if (isEmpty(model.getScm().getConnection())) {
                logError("scm.connection");
            }
            if (isEmpty(model.getScm().getDeveloperConnection())) {
                logError("scm.developerConnection");
            }
            if (isEmpty(model.getScm().getUrl())) {
                logError("scm.url");
            }
        }

        if (errorFound && !getIgnoreFailures()) {
            throw new GradleException(String.format(
                "POM validation failed for %s. Missing required elements: %s",
                getPomFile().getAsFile().get().getName(),
                String.join(", ", missingElements)
            ));
        }
    }

    private void logError(String element) {
        errorFound = true;
        missingElements.add(element);
        getLogger().error("No {} found in {}", element, getPomFile().getAsFile().get());
        try {
            ProblemId problemId = ProblemId.create("maven-pom", "Missing Element in Maven Pom", PROBLEM_GROUP);
            getProblems().getReporter().report(problemId, problemSpec -> {
                problemSpec.fileLocation(getPomFile().getAsFile().get().getPath());
                problemSpec.details("No " + element + " found");
                problemSpec.severity(Severity.ERROR);
            });
        } catch (LinkageError e) {
            // https://github.com/freefair/gradle-plugins/issues/1299
            getLogger().info("Incompatible Gradle Version", e);
        }
    }

    private boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    private boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    @Override
    public void setIgnoreFailures(boolean ignoreFailures) {
        this.ignoreFailures.set(ignoreFailures);
    }

    @Override
    public boolean getIgnoreFailures() {
        return ignoreFailures.get();
    }
}
