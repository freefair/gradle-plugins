package io.freefair.gradle.util;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TaskUtilsTest {

    Project project;
    static ObjectFactory objectFactory;

    @BeforeEach
    public void setUp() {
        project = ProjectBuilder.builder().build();
        objectFactory = project.getObjects();
    }

    @Test
    void registerNestedClass() throws InvocationTargetException, IllegalAccessException {

        Task task = project.getTasks().register("dummyTask", DefaultTask.class).get();
        TaskUtils.registerNested(task, new DummyClass(), "test");

        assertThat(task.getInputs().getProperties()).containsKeys("test.getFoo", "test.getBar");
    }

    @Test
    void registerNestedInterface() throws InvocationTargetException, IllegalAccessException {

        DefaultTask task = project.getTasks().register("dummyTask", DefaultTask.class).get();
        DummyInterface object = objectFactory.newInstance(DummyInterface.class);

        TaskUtils.registerNested(task, DummyInterface.class, object, "test");


        assertThat(task.getInputs().getProperties()).containsKeys("test.getFoo", "test.getBar");
    }

    public static class DummyClass {

        @Input
        public String getFoo() {
            return "foo";
        }

        @Input
        public Property<String> getBar() {
            return objectFactory.property(String.class).value("bar");
        }

    }

    public interface DummyInterface {

        @Input
        Property<Integer> getFoo();

        @Input
        Property<String> getBar();

    }
}
