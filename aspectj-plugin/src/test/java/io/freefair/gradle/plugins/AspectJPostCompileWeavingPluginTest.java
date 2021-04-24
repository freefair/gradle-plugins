package io.freefair.gradle.plugins;

import io.freefair.gradle.plugins.aspectj.AspectJPostCompileWeavingPlugin;
import org.gradle.api.*;
import org.gradle.api.plugins.GroovyPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.scala.ScalaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class AspectJPostCompileWeavingPluginTest {

    private Project project;

    @BeforeEach
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @ParameterizedTest
    @MethodSource("providePluginsForCompileIsApplied")
    public void compileIsApplied(List<Class<Plugin<Project>>> pluginClasses, int expectedNumberOfTaskAjcIsAddedTo) {
        pluginClasses.forEach(pluginClass -> project.getPlugins().apply(pluginClass));

        JavaPluginConvention convention = project.getConvention().getPlugin(JavaPluginConvention.class);
        convention.getSourceSets().create("foo");
        convention.getSourceSets().create("foo2");
        convention.getSourceSets().forEach(sourceSet -> {

            if (pluginClasses.contains(JavaPlugin.class)) {
                assertAjcForSourceSet(project, sourceSet.getCompileJavaTaskName());
            }

            if (pluginClasses.contains(GroovyPlugin.class)) {
                assertAjcForSourceSet(project, sourceSet.getCompileTaskName("groovy"));
            }

            if (pluginClasses.contains(ScalaPlugin.class)) {
                assertAjcForSourceSet(project, sourceSet.getCompileTaskName("scala"));
            }
        });
        Assertions.assertEquals(expectedNumberOfTaskAjcIsAddedTo, project.getTasks().stream().filter(AspectJPostCompileWeavingPluginTest::isAjcActionAdded).count());
    }

    private static Stream<Arguments> providePluginsForCompileIsApplied() {
        return Stream.of(
                Arguments.of(Arrays.asList(AspectJPostCompileWeavingPlugin.class), 0),
                Arguments.of(Arrays.asList(AspectJPostCompileWeavingPlugin.class, JavaPlugin.class), 4),
                Arguments.of(Arrays.asList(JavaPlugin.class, AspectJPostCompileWeavingPlugin.class), 4),
                Arguments.of(Arrays.asList(AspectJPostCompileWeavingPlugin.class, GroovyPlugin.class), 8),
                Arguments.of(Arrays.asList(GroovyPlugin.class, AspectJPostCompileWeavingPlugin.class), 8),
                Arguments.of(Arrays.asList(AspectJPostCompileWeavingPlugin.class, ScalaPlugin.class), 8),
                Arguments.of(Arrays.asList(ScalaPlugin.class, AspectJPostCompileWeavingPlugin.class), 8),
                Arguments.of(Arrays.asList(AspectJPostCompileWeavingPlugin.class, JavaPlugin.class, GroovyPlugin.class), 8),
                Arguments.of(Arrays.asList(AspectJPostCompileWeavingPlugin.class, GroovyPlugin.class, JavaPlugin.class), 8),
                Arguments.of(Arrays.asList(ScalaPlugin.class, AspectJPostCompileWeavingPlugin.class, GroovyPlugin.class, JavaPlugin.class), 12)
        );
    }

    private static boolean isAjcActionAdded(Task task) {
        List<Action<? super Task>> actions = task.getActions();
        return actions.stream().anyMatch(action -> ((Describable) action).getDisplayName().contains("ajc"));
    }

    private static void assertAjcForSourceSet(Project project, String taskName) {
        Assertions.assertTrue(isAjcActionAdded(project.getTasks().getByName(taskName)), "Missing expected AjcAction");
    }
}
