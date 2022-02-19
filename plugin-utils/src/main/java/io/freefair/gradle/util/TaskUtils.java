package io.freefair.gradle.util;

import lombok.experimental.UtilityClass;
import org.gradle.api.Task;
import org.gradle.api.tasks.*;

import java.lang.reflect.*;

@UtilityClass
public class TaskUtils {

    public static <T> void registerNested(Task task, T object, String prefix) throws InvocationTargetException, IllegalAccessException {
        Class<T> type = (Class<T>) object.getClass();
        registerNested(task, type, object, prefix);
    }

    public static <T> void registerNested(Task task, Class<? super T> type, T object, String prefix) throws InvocationTargetException, IllegalAccessException {

        while (!type.equals(Object.class)) {

            for (Field declaredField : type.getDeclaredFields()) {
                registerNested(task, declaredField, object, prefix);
            }

            for (Method declaredMethod : type.getDeclaredMethods()) {
                if (declaredMethod.getParameterCount() == 0) {
                    registerNested(task, declaredMethod, object, prefix);
                }
            }

            type = type.getSuperclass();
        }

    }

    private static <M extends Member & AnnotatedElement> void registerNested(Task task, M member, Object object, String prefix) throws IllegalAccessException, InvocationTargetException {
        if (member.isSynthetic()) {
            return;
        }
        else if (member.isAnnotationPresent(Internal.class)) {
            return;
        }

        Object value;
        if (member instanceof Field) {
            ((Field) member).setAccessible(true);
            value = ((Field) member).get(object);
        }
        else if (member instanceof Method) {
            value = ((Method) member).invoke(object);
        }
        else {
            throw new IllegalArgumentException();
        }

        String name = prefix + "." + member.getName();
        boolean optional = isOptional(member);
        boolean skipWhenEmpty = isSkipWhenEmpty(member);

        if (member.isAnnotationPresent(Input.class)) {
            task.getInputs().property(name, value)
                    .optional(optional);
        }

        if (member.isAnnotationPresent(InputFile.class)) {
            TaskInputFilePropertyBuilder inputFilePropertyBuilder = task.getInputs().file(value)
                    .withPropertyName(name)
                    .skipWhenEmpty(skipWhenEmpty)
                    .optional(optional);
            handlePathSensitivity(inputFilePropertyBuilder, member);
        }

        if (member.isAnnotationPresent(InputFiles.class)) {
            TaskInputFilePropertyBuilder inputFilePropertyBuilder = task.getInputs().files(value)
                    .withPropertyName(name)
                    .skipWhenEmpty(skipWhenEmpty)
                    .optional(optional);
            handlePathSensitivity(inputFilePropertyBuilder, member);
        }

        if (member.isAnnotationPresent(InputDirectory.class)) {
            TaskInputFilePropertyBuilder inputFilePropertyBuilder = task.getInputs().dir(value)
                    .withPropertyName(name)
                    .skipWhenEmpty(skipWhenEmpty)
                    .optional(optional);
            handlePathSensitivity(inputFilePropertyBuilder, member);
        }

        if (member.isAnnotationPresent(Classpath.class)) {
            task.getInputs().files(value)
                    .withPropertyName(name)
                    .skipWhenEmpty(skipWhenEmpty)
                    .withNormalizer(ClasspathNormalizer.class)
                    .optional(optional);
        }

        if (member.isAnnotationPresent(CompileClasspath.class)) {
            task.getInputs().files(value)
                    .withPropertyName(name)
                    .skipWhenEmpty(skipWhenEmpty)
                    .withNormalizer(CompileClasspathNormalizer.class)
                    .optional(optional);
        }

        if (member.isAnnotationPresent(OutputFile.class)) {
            task.getOutputs().file(value)
                    .withPropertyName(name)
                    .optional(optional);
        }

        if (member.isAnnotationPresent(OutputFiles.class)) {
            task.getOutputs().files(value)
                    .withPropertyName(name)
                    .optional(optional);
        }

        if (member.isAnnotationPresent(OutputDirectory.class)) {
            task.getOutputs().dir(value)
                    .withPropertyName(name)
                    .optional(optional);
        }

        if (member.isAnnotationPresent(OutputDirectories.class)) {
            task.getOutputs().dirs(value)
                    .withPropertyName(name)
                    .optional(optional);
        }

        if (member.isAnnotationPresent(Destroys.class)) {
            task.getDestroyables().register(value);
        }

        if (member.isAnnotationPresent(Nested.class)) {
            registerNested(task, value, name);
        }
    }

    private static void handlePathSensitivity(TaskInputFilePropertyBuilder propertyBuilder, AnnotatedElement element) {
        PathSensitive pathSensitive = element.getAnnotation(PathSensitive.class);

        if (pathSensitive != null) {
            propertyBuilder.withPathSensitivity(pathSensitive.value());
        }
    }

    private static <M extends Member & AnnotatedElement> boolean isSkipWhenEmpty(M member) {
        return member.isAnnotationPresent(SkipWhenEmpty.class);
    }

    private static <M extends Member & AnnotatedElement> boolean isOptional(M member) {
        return member.isAnnotationPresent(Optional.class);
    }


}
