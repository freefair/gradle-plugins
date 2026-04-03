package io.freefair.gradle.plugins.settings;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;

import java.net.URL;
import java.util.List;

/**
 * Settings plugin that automatically registers all freefair Gradle plugins with their versions.
 * <p>
 * This plugin reads plugin IDs from {@code META-INF/freefair/plugin-ids} and registers them
 * in the settings plugin management block, setting each plugin's version to match this
 * plugin's package version. This allows using freefair plugins without explicitly
 * specifying version numbers in the plugins block.
 * <p>
 * Apply this plugin in {@code settings.gradle}:
 * <pre>
 * plugins {
 *     id 'io.freefair.plugin-versions' version 'x.y.z'
 * }
 * </pre>
 */
@Slf4j
public class PluginVersionsPlugin implements Plugin<Settings> {
    @SneakyThrows
    @Override
    public void apply(Settings settings) {

        String version = this.getClass().getPackage().getImplementationVersion();

        URL resource = this.getClass().getClassLoader().getResource("META-INF/freefair/plugin-ids");

        List<String> ids = ResourceGroovyMethods.readLines(resource);

        settings.getPluginManagement().plugins(pds -> {
            for (String id : ids) {
                log.info("id '{}' version '{}'", id, version);
                pds.id(id).version(version);
            }
        });

    }
}
