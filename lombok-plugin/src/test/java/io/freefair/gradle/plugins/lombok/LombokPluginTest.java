package io.freefair.gradle.plugins.lombok;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonarqube.gradle.SonarQubePlugin;

import static org.assertj.core.api.Assertions.assertThat;

public class LombokPluginTest {

    private Project project;

    @BeforeEach
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void apply_alone() {
        project.getPlugins().apply(LombokPlugin.class);
    }

    @Test
    public void apply_after_java() {
        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(LombokPlugin.class);
    }

    @Test
    public void apply_before_java() {
        project.getPlugins().apply(LombokPlugin.class);
        project.getPlugins().apply(JavaPlugin.class);
    }

    @Test
    public void configureForSpotbugs_toleratesEarlyResolution() {
        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(LombokPlugin.class);
        project.getPlugins().apply(SonarQubePlugin.class);

        // Simulate the Gradle Tooling API resolving compileClasspath (which extends
        // compileOnly) during the configuration phase, before afterEvaluate fires.
        // With the old afterEvaluate { add() } approach, resolving compileClasspath
        // would mark compileOnly as immutable, and the subsequent add() in afterEvaluate
        // would throw:
        //   "Cannot change dependencies of dependency configuration ':compileOnly'
        //    after it has been included in dependency resolution"
        // The withDependencies fix adds spotbugs-annotations lazily, just before
        // the configuration is resolved, avoiding the immutability conflict entirely.
        Configuration compileClasspath = project.getConfigurations().getByName("compileClasspath");
        var unresolvedDeps = compileClasspath.getResolvedConfiguration()
                .getLenientConfiguration().getUnresolvedModuleDependencies();

        assertThat(unresolvedDeps)
                .anyMatch(d -> "spotbugs-annotations".equals(d.getSelector().getName()));
    }

    @Test
    public void with_sonar() {
        project.getPlugins().apply("java");
        project.getPlugins().apply(LombokPlugin.class);

        LombokExtension lombokExtension = project.getExtensions().getByType(LombokExtension.class);

        assertThat(lombokExtension).isNotNull();

        project.getPlugins().apply(SonarQubePlugin.class);
    }
}
